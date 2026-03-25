package ai.practice.summarizer

import ai.practice.domain.BlogPost
import ai.practice.domain.BlogSource
import ai.practice.domain.SourceType
import ai.practice.repository.BlogPostRepository
import ai.practice.repository.BlogSourceRepository
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.context.annotation.Import
import kotlin.test.assertEquals
import kotlin.test.assertNull

@DataJpaTest
class SummaryUpdateServiceTest @Autowired constructor(
    private val blogPostRepository: BlogPostRepository,
    private val blogSourceRepository: BlogSourceRepository,
    private val entityManager: EntityManager
) {

    private val postSaver = object : PostSaver(blogPostRepository) {
        override fun save(post: BlogPost, result: SummarizeResult) {
            post.summary = result.summary
            post.tags = result.tags.joinToString(",")
            blogPostRepository.save(post)
        }
    }

    private lateinit var savedSource: BlogSource

    @BeforeEach
    fun setUp() {
        savedSource = blogSourceRepository.save(
            BlogSource(name = "테스트", url = "https://test.com", type = SourceType.RSS)
        )
    }

    private fun createService(summarizer: Summarizer) =
        SummaryUpdateService(blogPostRepository, summarizer, postSaver)

    @Test
    fun `content가 있는 글만 요약을 수행한다`() {
        blogPostRepository.save(BlogPost(blogSource = savedSource, title = "글1", content = "본문 있음", url = "https://test.com/1"))
        blogPostRepository.save(BlogPost(blogSource = savedSource, title = "글2", content = null, url = "https://test.com/2"))
        blogPostRepository.save(BlogPost(blogSource = savedSource, title = "글3", content = "  ", url = "https://test.com/3"))
        entityManager.flush()
        entityManager.clear()

        val called = mutableListOf<String>()
        val service = createService(object : Summarizer {
            override fun summarize(title: String, content: String): SummarizeResult {
                called.add(title)
                return SummarizeResult("요약: $title", listOf("백엔드"))
            }
        })

        service.updateAllSummaries()

        assertEquals(listOf("글1"), called)
        entityManager.flush()
        entityManager.clear()
        val post = blogPostRepository.findAll().first { it.title == "글1" }
        assertEquals("요약: 글1", post.summary)
        assertEquals("백엔드", post.tags)
    }

    @Test
    fun `summarizer가 null을 반환하면 요약이 업데이트되지 않는다`() {
        blogPostRepository.save(BlogPost(blogSource = savedSource, title = "글1", content = "본문", url = "https://test.com/4"))
        entityManager.flush()
        entityManager.clear()

        val service = createService(object : Summarizer {
            override fun summarize(title: String, content: String): SummarizeResult? = null
        })

        service.updateAllSummaries()

        entityManager.clear()
        val post = blogPostRepository.findAll().first()
        assertNull(post.summary)
        assertNull(post.tags)
    }

    @Test
    fun `글이 없으면 아무 일도 일어나지 않는다`() {
        val called = mutableListOf<String>()
        val service = createService(object : Summarizer {
            override fun summarize(title: String, content: String): SummarizeResult {
                called.add(title)
                return SummarizeResult("요약", emptyList())
            }
        })

        service.updateAllSummaries()

        assertEquals(emptyList<String>(), called)
    }
}
