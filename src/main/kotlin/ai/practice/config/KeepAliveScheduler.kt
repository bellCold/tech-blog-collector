package ai.practice.config

import jakarta.persistence.EntityManager
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class KeepAliveScheduler(
    private val entityManager: EntityManager
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedRate = 4 * 60 * 1000)
    fun pingDatabase() {
        try {
            entityManager.createNativeQuery("SELECT 1").singleResult
            log.debug("Database keep-alive ping successful")
        } catch (e: Exception) {
            log.warn("Database keep-alive ping failed: ${e.message}")
        }
    }
}
