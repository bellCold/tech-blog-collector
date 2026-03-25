package ai.practice.summarizer

import ai.practice.domain.BlogPost
import ai.practice.repository.BlogPostRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class SummaryUpdateService(
    private val blogPostRepository: BlogPostRepository,
    private val summarizer: Summarizer,
    private val postSaver: PostSaver
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Async
    fun updateAllSummaries() {
        var updated = 0
        var page = 0
        do {
            val posts = blogPostRepository.findAll(PageRequest.of(page, 20))
            for (post in posts.content) {
                if (!post.content.isNullOrBlank()) {
                    val result = summarizer.summarize(post.title, post.content!!)
                    if (result != null) {
                        postSaver.save(post, result)
                        updated++
                        log.info("Updated [$updated]: ${post.title} → tags: ${result.tags}")
                    }
                }
            }
            page++
        } while (posts.hasNext())

        log.info("Summary update finished: $updated posts updated")
    }
}

@Service
class PostSaver(
    private val blogPostRepository: BlogPostRepository
) {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun save(post: BlogPost, result: SummarizeResult) {
        post.summary = result.summary
        post.tags = result.tags.joinToString(",")
        blogPostRepository.save(post)
    }
}
