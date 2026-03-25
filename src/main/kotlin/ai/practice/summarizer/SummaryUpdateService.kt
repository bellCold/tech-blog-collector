package ai.practice.summarizer

import ai.practice.repository.BlogPostRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SummaryUpdateService(
    private val blogPostRepository: BlogPostRepository,
    private val summarizer: Summarizer
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun updateAllSummaries(): Int {
        var updated = 0
        var page = 0
        do {
            val posts = blogPostRepository.findAll(PageRequest.of(page, 20))
            for (post in posts.content) {
                if (!post.content.isNullOrBlank()) {
                    val newSummary = summarizer.summarize(post.title, post.content!!)
                    if (newSummary != null) {
                        post.summary = newSummary
                        updated++
                        log.info("Updated summary [$updated]: ${post.title}")
                    }
                }
            }
            page++
        } while (posts.hasNext())

        log.info("Summary update finished: $updated posts updated")
        return updated
    }
}
