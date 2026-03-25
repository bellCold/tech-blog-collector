package ai.practice.collector

import ai.practice.domain.BlogPost
import ai.practice.domain.BlogSource
import ai.practice.domain.SourceType
import ai.practice.repository.BlogPostRepository
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class WebCrawlCollector(
    private val blogPostRepository: BlogPostRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val USER_AGENT = "TechBlogAggregator/1.0"
        private const val TIMEOUT_MS = 10_000
        private const val DELAY_MS = 1000L
    }

    fun collect(source: BlogSource): List<BlogPost> {
        if (source.type != SourceType.CRAWL) return emptyList()

        val listSelector = source.listSelector ?: return emptyList()
        val titleSelector = source.titleSelector ?: "h1"
        val contentSelector = source.contentSelector ?: "article"

        return try {
            val listDoc = Jsoup.connect(source.url)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .get()

            val links = listDoc.select(listSelector)
                .mapNotNull { it.attr("abs:href").takeIf { url -> url.isNotBlank() } }

            val newPosts = links.mapNotNull { postUrl ->
                if (blogPostRepository.existsByUrl(postUrl)) return@mapNotNull null

                Thread.sleep(DELAY_MS)

                try {
                    val postDoc = Jsoup.connect(postUrl)
                        .userAgent(USER_AGENT)
                        .timeout(TIMEOUT_MS)
                        .get()

                    val title = postDoc.select(titleSelector).text().takeIf { it.isNotBlank() } ?: "Untitled"
                    val content = postDoc.select(contentSelector).text()

                    BlogPost(
                        blogSource = source,
                        title = title,
                        content = content,
                        summary = content.take(200),
                        url = postUrl,
                        author = source.name
                    )
                } catch (e: Exception) {
                    log.warn("Failed to crawl post: $postUrl - ${e.message}")
                    null
                }
            }

            blogPostRepository.saveAll(newPosts)
            log.info("Crawled ${newPosts.size} new posts from ${source.name}")
            newPosts
        } catch (e: Exception) {
            log.error("Failed to crawl ${source.name}: ${e.message}", e)
            emptyList()
        }
    }
}
