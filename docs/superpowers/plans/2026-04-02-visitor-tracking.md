# Visitor Tracking Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** IP 기반 방문자수 추적 기능 추가 (오늘 방문자수 + 총 방문자수)

**Architecture:** VisitorLog 엔티티에 IP + 날짜를 저장하고, UNIQUE 제약으로 하루 1회만 기록. 프론트에서 POST로 방문 기록, GET으로 조회.

**Tech Stack:** Spring Boot 4.0.4, Kotlin, Spring Data JPA, PostgreSQL

---

## File Structure

| Action | Path | Responsibility |
|--------|------|----------------|
| Create | `src/main/kotlin/ai/practice/domain/VisitorLog.kt` | JPA 엔티티 |
| Create | `src/main/kotlin/ai/practice/repository/VisitorLogRepository.kt` | 데이터 접근 |
| Create | `src/main/kotlin/ai/practice/dto/VisitorDto.kt` | 응답 DTO |
| Create | `src/main/kotlin/ai/practice/service/VisitorService.kt` | 방문 기록/조회 로직 |
| Create | `src/main/kotlin/ai/practice/controller/VisitorController.kt` | REST 엔드포인트 |
| Create | `src/test/kotlin/ai/practice/repository/VisitorLogRepositoryTest.kt` | 레포지토리 테스트 |
| Create | `src/test/kotlin/ai/practice/service/VisitorServiceTest.kt` | 서비스 테스트 |

---

### Task 1: VisitorLog 엔티티 + 레포지토리

**Files:**
- Create: `src/main/kotlin/ai/practice/domain/VisitorLog.kt`
- Create: `src/main/kotlin/ai/practice/repository/VisitorLogRepository.kt`
- Create: `src/test/kotlin/ai/practice/repository/VisitorLogRepositoryTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
// src/test/kotlin/ai/practice/repository/VisitorLogRepositoryTest.kt
package ai.practice.repository

import ai.practice.domain.VisitorLog
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertAll

@DataJpaTest
class VisitorLogRepositoryTest @Autowired constructor(
    private val visitorLogRepository: VisitorLogRepository,
    private val entityManager: EntityManager
) {

    private fun flushAndClear() {
        entityManager.flush()
        entityManager.clear()
    }

    @BeforeEach
    fun setUp() {
        visitorLogRepository.deleteAll()
        flushAndClear()
    }

    @Test
    fun `VisitorLog를 저장하고 조회한다`() {
        val log = visitorLogRepository.save(
            VisitorLog(ipAddress = "192.168.1.1", visitDate = LocalDate.now())
        )
        flushAndClear()

        val found = visitorLogRepository.findById(log.id).orElse(null)

        assertAll(
            { assertNotNull(found) },
            { assertEquals("192.168.1.1", found.ipAddress) },
            { assertEquals(LocalDate.now(), found.visitDate) },
            { assertNotNull(found.createdAt) }
        )
    }

    @Test
    fun `같은 IP와 날짜로 중복 존재 여부를 확인한다`() {
        visitorLogRepository.save(
            VisitorLog(ipAddress = "192.168.1.1", visitDate = LocalDate.now())
        )
        flushAndClear()

        assertTrue(visitorLogRepository.existsByIpAddressAndVisitDate("192.168.1.1", LocalDate.now()))
    }

    @Test
    fun `오늘 방문자수를 카운트한다`() {
        val today = LocalDate.now()
        visitorLogRepository.save(VisitorLog(ipAddress = "1.1.1.1", visitDate = today))
        visitorLogRepository.save(VisitorLog(ipAddress = "2.2.2.2", visitDate = today))
        visitorLogRepository.save(VisitorLog(ipAddress = "3.3.3.3", visitDate = today.minusDays(1)))
        flushAndClear()

        assertEquals(2, visitorLogRepository.countByVisitDate(today))
    }

    @Test
    fun `총 방문자수를 유니크 IP로 카운트한다`() {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        visitorLogRepository.save(VisitorLog(ipAddress = "1.1.1.1", visitDate = today))
        visitorLogRepository.save(VisitorLog(ipAddress = "1.1.1.1", visitDate = yesterday))
        visitorLogRepository.save(VisitorLog(ipAddress = "2.2.2.2", visitDate = today))
        flushAndClear()

        assertEquals(2, visitorLogRepository.countDistinctIpAddress())
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew test --tests "ai.practice.repository.VisitorLogRepositoryTest"`
Expected: FAIL — classes don't exist yet

- [ ] **Step 3: Write VisitorLog entity**

```kotlin
// src/main/kotlin/ai/practice/domain/VisitorLog.kt
package ai.practice.domain

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(
    name = "visitor_log",
    uniqueConstraints = [UniqueConstraint(columnNames = ["ip_address", "visit_date"])],
    indexes = [Index(name = "idx_visitor_log_visit_date", columnList = "visit_date")]
)
class VisitorLog(
    @Column(name = "ip_address", nullable = false, length = 45)
    val ipAddress: String,

    @Column(name = "visit_date", nullable = false)
    val visitDate: LocalDate,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
```

- [ ] **Step 4: Write VisitorLogRepository**

```kotlin
// src/main/kotlin/ai/practice/repository/VisitorLogRepository.kt
package ai.practice.repository

import ai.practice.domain.VisitorLog
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

interface VisitorLogRepository : JpaRepository<VisitorLog, Long> {
    fun existsByIpAddressAndVisitDate(ipAddress: String, visitDate: LocalDate): Boolean
    fun countByVisitDate(visitDate: LocalDate): Long

    @Query("SELECT COUNT(DISTINCT v.ipAddress) FROM VisitorLog v")
    fun countDistinctIpAddress(): Long
}
```

- [ ] **Step 5: Run tests to verify they pass**

Run: `./gradlew test --tests "ai.practice.repository.VisitorLogRepositoryTest"`
Expected: All 4 tests PASS

- [ ] **Step 6: Commit**

```bash
git add src/main/kotlin/ai/practice/domain/VisitorLog.kt src/main/kotlin/ai/practice/repository/VisitorLogRepository.kt src/test/kotlin/ai/practice/repository/VisitorLogRepositoryTest.kt
git commit -m "feat: VisitorLog 엔티티 및 레포지토리 추가"
```

---

### Task 2: DTO + Service

**Files:**
- Create: `src/main/kotlin/ai/practice/dto/VisitorDto.kt`
- Create: `src/main/kotlin/ai/practice/service/VisitorService.kt`
- Create: `src/test/kotlin/ai/practice/service/VisitorServiceTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
// src/test/kotlin/ai/practice/service/VisitorServiceTest.kt
package ai.practice.service

import ai.practice.domain.VisitorLog
import ai.practice.repository.VisitorLogRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertAll

@ExtendWith(MockitoExtension::class)
class VisitorServiceTest {

    @Mock
    lateinit var visitorLogRepository: VisitorLogRepository

    @InjectMocks
    lateinit var visitorService: VisitorService

    @Test
    fun `신규 방문자를 기록한다`() {
        val ip = "1.1.1.1"
        whenever(visitorLogRepository.existsByIpAddressAndVisitDate(ip, LocalDate.now())).thenReturn(false)
        whenever(visitorLogRepository.save(any<VisitorLog>())).thenAnswer { it.arguments[0] }

        visitorService.recordVisit(ip)

        verify(visitorLogRepository).save(any<VisitorLog>())
    }

    @Test
    fun `이미 방문한 IP는 저장하지 않는다`() {
        val ip = "1.1.1.1"
        whenever(visitorLogRepository.existsByIpAddressAndVisitDate(ip, LocalDate.now())).thenReturn(true)

        visitorService.recordVisit(ip)

        verify(visitorLogRepository, never()).save(any<VisitorLog>())
    }

    @Test
    fun `방문자 통계를 조회한다`() {
        whenever(visitorLogRepository.countByVisitDate(LocalDate.now())).thenReturn(10)
        whenever(visitorLogRepository.countDistinctIpAddress()).thenReturn(100)

        val response = visitorService.getVisitorStats()

        assertAll(
            { assertEquals(10, response.todayCount) },
            { assertEquals(100, response.totalCount) }
        )
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew test --tests "ai.practice.service.VisitorServiceTest"`
Expected: FAIL — VisitorService and VisitorDto don't exist

- [ ] **Step 3: Write VisitorDto**

```kotlin
// src/main/kotlin/ai/practice/dto/VisitorDto.kt
package ai.practice.dto

data class VisitorResponse(
    val todayCount: Long,
    val totalCount: Long
)
```

- [ ] **Step 4: Write VisitorService**

```kotlin
// src/main/kotlin/ai/practice/service/VisitorService.kt
package ai.practice.service

import ai.practice.domain.VisitorLog
import ai.practice.dto.VisitorResponse
import ai.practice.repository.VisitorLogRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class VisitorService(
    private val visitorLogRepository: VisitorLogRepository
) {

    @Transactional
    fun recordVisit(ipAddress: String) {
        val today = LocalDate.now()
        if (!visitorLogRepository.existsByIpAddressAndVisitDate(ipAddress, today)) {
            visitorLogRepository.save(VisitorLog(ipAddress = ipAddress, visitDate = today))
        }
    }

    fun getVisitorStats(): VisitorResponse {
        val todayCount = visitorLogRepository.countByVisitDate(LocalDate.now())
        val totalCount = visitorLogRepository.countDistinctIpAddress()
        return VisitorResponse(todayCount = todayCount, totalCount = totalCount)
    }
}
```

- [ ] **Step 5: Run tests to verify they pass**

Run: `./gradlew test --tests "ai.practice.service.VisitorServiceTest"`
Expected: All 3 tests PASS

- [ ] **Step 6: Commit**

```bash
git add src/main/kotlin/ai/practice/dto/VisitorDto.kt src/main/kotlin/ai/practice/service/VisitorService.kt src/test/kotlin/ai/practice/service/VisitorServiceTest.kt
git commit -m "feat: VisitorService 및 DTO 추가"
```

---

### Task 3: Controller

**Files:**
- Create: `src/main/kotlin/ai/practice/controller/VisitorController.kt`

- [ ] **Step 1: Write VisitorController**

```kotlin
// src/main/kotlin/ai/practice/controller/VisitorController.kt
package ai.practice.controller

import ai.practice.dto.VisitorResponse
import ai.practice.service.VisitorService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/visitors")
class VisitorController(
    private val visitorService: VisitorService
) {

    @PostMapping
    fun recordVisit(request: HttpServletRequest): ResponseEntity<Void> {
        val ipAddress = extractIpAddress(request)
        visitorService.recordVisit(ipAddress)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @GetMapping
    fun getVisitorStats(): VisitorResponse {
        return visitorService.getVisitorStats()
    }

    private fun extractIpAddress(request: HttpServletRequest): String {
        val forwarded = request.getHeader("X-Forwarded-For")
        return if (!forwarded.isNullOrBlank()) {
            forwarded.split(",").first().trim()
        } else {
            request.remoteAddr
        }
    }
}
```

- [ ] **Step 2: Run all tests**

Run: `./gradlew test`
Expected: All tests PASS (기존 테스트 포함)

- [ ] **Step 3: Commit**

```bash
git add src/main/kotlin/ai/practice/controller/VisitorController.kt
git commit -m "feat: VisitorController 추가 (POST/GET /api/visitors)"
```

---

### Task 4: 빌드 검증 + SPEC 업데이트

**Files:**
- Modify: `SPEC.md`

- [ ] **Step 1: 전체 빌드 확인**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 2: SPEC.md에 방문자 API 스펙 추가**

SPEC.md의 API 엔드포인트 섹션에 추가:

```markdown
### 방문자 `/api/visitors`
| Method | Path | 설명 | Response |
|--------|------|------|----------|
| POST | / | 방문 기록 (IP 기반, 하루 1회) | 201 |
| GET | / | 오늘 방문자수 + 총 방문자수 | VisitorResponse |
```

DTO 섹션에 추가:

```markdown
**VisitorResponse**: `todayCount, totalCount`
```

도메인 모델 섹션에 VisitorLog 추가.

- [ ] **Step 3: Commit**

```bash
git add SPEC.md
git commit -m "docs: SPEC.md에 방문자 추적 API 스펙 추가"
```
