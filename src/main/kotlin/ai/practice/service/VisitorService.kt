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
        val today = LocalDate.now()
        val todayCount = visitorLogRepository.countByVisitDate(today)
        val yesterdayCount = visitorLogRepository.countByVisitDate(today.minusDays(1))
        val totalCount = visitorLogRepository.countDistinctIpAddress()
        return VisitorResponse(todayCount = todayCount, yesterdayCount = yesterdayCount, totalCount = totalCount)
    }
}
