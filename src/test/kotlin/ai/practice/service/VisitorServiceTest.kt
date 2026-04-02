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
import org.junit.jupiter.api.assertAll

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
        val today = LocalDate.now()
        whenever(visitorLogRepository.countByVisitDate(today)).thenReturn(10)
        whenever(visitorLogRepository.countByVisitDate(today.minusDays(1))).thenReturn(50)
        whenever(visitorLogRepository.countDistinctIpAddress()).thenReturn(100)

        val response = visitorService.getVisitorStats()

        assertAll(
            { assertEquals(10, response.todayCount) },
            { assertEquals(50, response.yesterdayCount) },
            { assertEquals(100, response.totalCount) }
        )
    }
}
