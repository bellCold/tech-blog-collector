package ai.practice.controller

import jakarta.persistence.EntityManager
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthController(
    private val entityManager: EntityManager
) {
    @GetMapping("/api/health")
    fun health(): Map<String, String> {
        entityManager.createNativeQuery("SELECT 1").singleResult
        return mapOf("status" to "ok")
    }
}
