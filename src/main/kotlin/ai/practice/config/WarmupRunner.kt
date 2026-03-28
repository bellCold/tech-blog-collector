package ai.practice.config

import ai.practice.repository.BlogPostRepository
import ai.practice.repository.BlogSourceRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component

@Component
class WarmupRunner(
    private val blogSourceRepository: BlogSourceRepository,
    private val blogPostRepository: BlogPostRepository
) : ApplicationRunner {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun run(args: ApplicationArguments) {
        log.info("Warmup started")
        try {
            blogSourceRepository.findAll()
            blogPostRepository.findAll(PageRequest.of(0, 1))
            blogPostRepository.existsByUrl("")
            log.info("Warmup completed")
        } catch (e: Exception) {
            log.warn("Warmup failed: ${e.message}")
        }
    }
}
