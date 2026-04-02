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
