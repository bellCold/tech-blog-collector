package ai.practice.repository

import ai.practice.domain.VisitorLog
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.assertAll

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
