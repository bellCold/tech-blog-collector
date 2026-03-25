package ai.practice.collector

import ai.practice.domain.BlogPost
import ai.practice.domain.BlogSource
import ai.practice.repository.BlogPostRepository
import com.apptasticsoftware.rssreader.RssReader
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Component
class RssCollector(
    private val blogPostRepository: BlogPostRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val rssReader = RssReader()

    fun collect(source: BlogSource): List<BlogPost> {
        val rssUrl = source.rssUrl ?: return emptyList()

        return try {
            val items = rssReader.read(rssUrl).toList()
            val newPosts = items.mapNotNull { item ->
                val postUrl = item.link.orElse(null) ?: return@mapNotNull null
                if (blogPostRepository.existsByUrl(postUrl)) return@mapNotNull null

                val rawDescription = item.description.orElse(null)
                val cleanText = rawDescription?.let { Jsoup.parse(it).text() }

                BlogPost(
                    blogSource = source,
                    title = item.title.orElse("Untitled"),
                    content = cleanText,
                    summary = cleanText?.take(200),
                    url = postUrl,
                    author = item.author.orElse(null),
                    publishedAt = item.pubDate.orElse(null)?.let { parseDate(it) }
                )
            }

            blogPostRepository.saveAll(newPosts)
            log.info("Collected ${newPosts.size} new posts from ${source.name}")
            newPosts
        } catch (e: Exception) {
            log.error("Failed to collect RSS from ${source.name}: ${e.message}", e)
            emptyList()
        }
    }

    private fun parseDate(dateStr: String): LocalDateTime? {
        return try {
            ZonedDateTime.parse(dateStr, DateTimeFormatter.RFC_1123_DATE_TIME).toLocalDateTime()
        } catch (e: Exception) {
            try {
                ZonedDateTime.parse(dateStr, DateTimeFormatter.ISO_DATE_TIME).toLocalDateTime()
            } catch (e: Exception) {
                log.warn("Unparseable date: $dateStr")
                null
            }
        }
    }
}
