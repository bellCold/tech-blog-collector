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
