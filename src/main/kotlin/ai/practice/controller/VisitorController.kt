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
