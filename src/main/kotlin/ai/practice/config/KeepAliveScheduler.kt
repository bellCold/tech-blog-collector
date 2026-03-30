package ai.practice.config

import com.zaxxer.hikari.HikariDataSource
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import javax.sql.DataSource

@Component
class KeepAliveScheduler(
    private val dataSource: DataSource
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedRate = 2 * 60 * 1000)
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
            evictBrokenConnections()
        }
    }

    private fun evictBrokenConnections() {
        try {
            val hikariDs = dataSource as? HikariDataSource ?: return
            val pool = hikariDs.hikariPoolMXBean ?: return
            pool.softEvictConnections()
            log.info("Evicted broken connections from pool. total={}, active={}, idle={}, waiting={}",
                pool.totalConnections, pool.activeConnections, pool.idleConnections, pool.threadsAwaitingConnection)
        } catch (e: Exception) {
            log.warn("Failed to evict broken connections: ${e.message}")
        }
    }
}
