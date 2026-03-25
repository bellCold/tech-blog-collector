package ai.practice.collector

import ai.practice.domain.SourceType
import ai.practice.repository.BlogSourceRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class CollectorScheduler(
    private val blogSourceRepository: BlogSourceRepository,
    private val rssCollector: RssCollector,
    private val webCrawlCollector: WebCrawlCollector
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(cron = "\${collector.cron}")
    fun collectAll() {
        log.info("Starting scheduled collection...")
        val sources = blogSourceRepository.findAll()
        sources.forEach { source ->
            try {
                when (source.type) {
                    SourceType.RSS -> rssCollector.collect(source)
                    SourceType.CRAWL -> webCrawlCollector.collect(source)
                }
            } catch (e: Exception) {
                log.error("Failed to collect from ${source.name}: ${e.message}", e)
            }
        }
        log.info("Scheduled collection finished.")
    }

    fun collectBySourceId(sourceId: Long) {
        val source = blogSourceRepository.findById(sourceId).orElseThrow {
            IllegalArgumentException("BlogSource not found: id=$sourceId")
        }
        when (source.type) {
            SourceType.RSS -> rssCollector.collect(source)
            SourceType.CRAWL -> log.info("Crawl collector not yet implemented for ${source.name}")
        }
    }
}
