package ai.practice.collector

import ai.practice.domain.BlogPost
import ai.practice.domain.BlogSource
import ai.practice.repository.BlogPostRepository
import ai.practice.summarizer.Summarizer
import com.apptasticsoftware.rssreader.RssReader
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Component
class RssCollector(
    private val blogPostRepository: BlogPostRepository,
    private val summarizer: Summarizer
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val rssReader = RssReader()
    private val httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(15))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build()

    fun collect(source: BlogSource): List<BlogPost> {
        val rssUrl = source.rssUrl ?: return emptyList()

        return try {
            val xml = fetchRss(rssUrl) ?: return emptyList<BlogPost>().also {
                log.warn("Failed to fetch RSS from ${source.name}: empty response")
            }
            val cleanedXml = sanitizeXml(xml)
            val inputStream = ByteArrayInputStream(cleanedXml.toByteArray(Charsets.UTF_8))
            val items = rssReader.read(inputStream).toList()
            val newPosts = items.mapNotNull { item ->
                val rawUrl = item.link.orElse(null) ?: return@mapNotNull null
                val postUrl = if (rawUrl.startsWith("http")) rawUrl
                    else source.url.trimEnd('/') + "/" + rawUrl.trimStart('/')
                if (blogPostRepository.existsByUrl(postUrl)) return@mapNotNull null

                val rawDescription = item.description.orElse(null)
                val cleanText = rawDescription?.let { Jsoup.parse(it).text() }
                val title = item.title.orElse("Untitled")
                val result = if (!cleanText.isNullOrBlank()) {
                    summarizer.summarize(title, cleanText)
                } else null

                BlogPost(
                    blogSource = source,
                    title = title,
                    content = cleanText,
                    summary = result?.summary ?: cleanText?.take(200),
                    tags = result?.tags?.joinToString(","),
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

    private fun fetchRss(url: String): String? {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36")
            .header("Accept", "application/rss+xml, application/xml, text/xml, */*")
            .timeout(Duration.ofSeconds(30))
            .GET()
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() != 200) {
            log.warn("RSS fetch failed for $url: HTTP ${response.statusCode()}")
            return null
        }
        return response.body()
    }

    private fun sanitizeXml(xml: String): String {
        val sb = StringBuilder(xml.length)
        for (ch in xml) {
            if (ch == '\t' || ch == '\n' || ch == '\r' || ch in '\u0020'..'\uFFFD') {
                sb.append(ch)
            }
        }
        return sb.toString()
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
