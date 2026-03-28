package ai.practice.config

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import javax.sql.DataSource

@Component
class KeepAliveScheduler(
    private val dataSource: DataSource
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedRate = 4 * 60 * 1000)
    fun pingDatabase() {
        try {
            dataSource.connection.use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.execute("SELECT 1")
                }
            }
            log.debug("Database keep-alive ping successful")
        } catch (e: Exception) {
            log.warn("Database keep-alive ping failed: ${e.message}")
        }
    }
}
