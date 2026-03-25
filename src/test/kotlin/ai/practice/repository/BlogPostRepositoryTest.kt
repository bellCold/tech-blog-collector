package ai.practice.repository

import ai.practice.domain.BlogPost
import ai.practice.domain.BlogSource
import ai.practice.domain.SourceType
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.data.domain.PageRequest
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@DataJpaTest
class BlogPostRepositoryTest @Autowired constructor(
    private val blogPostRepository: BlogPostRepository,
    private val blogSourceRepository: BlogSourceRepository,
    private val entityManager: EntityManager
) {

    private lateinit var source1: BlogSource
    private lateinit var source2: BlogSource

    @BeforeEach
    fun setUp() {
        source1 = blogSourceRepository.save(
            BlogSource(
                name = "카카오 기술블로그",
                url = "https://tech.kakao.com",
                rssUrl = "https://tech.kakao.com/feed",
                type = SourceType.RSS
            )
        )
        source2 = blogSourceRepository.save(
            BlogSource(
                name = "네이버 D2",
                url = "https://d2.naver.com",
                type = SourceType.CRAWL
            )
        )
        flushAndClear()
    }

    private fun flushAndClear() {
        entityManager.flush()
        entityManager.clear()
    }

    private fun createPost(
        blogSource: BlogSource = source1,
        title: String = "테스트 글",
        url: String = "https://test.com/post/${System.nanoTime()}",
        content: String? = "테스트 본문",
        summary: String? = "테스트 요약",
        author: String? = "작성자",
        publishedAt: LocalDateTime? = LocalDateTime.now()
    ) = BlogPost(
        blogSource = blogSource,
        title = title,
        url = url,
        content = content,
        summary = summary,
        author = author,
        publishedAt = publishedAt
    )

    @Test
    fun `BlogPost를 저장하고 조회한다`() {
        val post = blogPostRepository.save(
            createPost(title = "Spring Boot 가이드", content = "스프링 부트 시작하기")
        )
        flushAndClear()

        val found = blogPostRepository.findById(post.id).orElse(null)

        assertNotNull(found)
        assertAll(
            { assertEquals("Spring Boot 가이드", found.title) },
            { assertEquals("스프링 부트 시작하기", found.content) },
            { assertNotNull(found.collectedAt) },
            { assertNotNull(found.createdAt) }
        )
    }

    @Test
    fun `소스별로 글 목록을 페이징 조회한다`() {
        repeat(5) { i ->
            blogPostRepository.save(createPost(blogSource = source1, title = "카카오 글 $i", url = "https://kakao.com/post/$i"))
        }
        repeat(3) { i ->
            blogPostRepository.save(createPost(blogSource = source2, title = "네이버 글 $i", url = "https://naver.com/post/$i"))
        }
        flushAndClear()

        val page = blogPostRepository.findByBlogSourceId(source1.id, PageRequest.of(0, 3))

        assertAll(
            { assertEquals(3, page.content.size) },
            { assertEquals(5, page.totalElements) },
            { assertEquals(2, page.totalPages) }
        )
    }

    @Test
    fun `키워드로 제목을 검색한다`() {
        blogPostRepository.save(createPost(title = "Kotlin 코루틴 가이드", url = "https://test.com/1"))
        blogPostRepository.save(createPost(title = "Java 동시성 프로그래밍", url = "https://test.com/2"))
        blogPostRepository.save(createPost(title = "Kotlin Flow 활용", url = "https://test.com/3"))
        flushAndClear()

        val page = blogPostRepository.searchByKeyword("Kotlin", PageRequest.of(0, 10))

        assertEquals(2, page.totalElements)
    }

    @Test
    fun `키워드로 본문을 검색한다`() {
        blogPostRepository.save(createPost(title = "제목1", content = "Spring WebFlux 소개", url = "https://test.com/1"))
        blogPostRepository.save(createPost(title = "제목2", content = "Spring MVC 가이드", url = "https://test.com/2"))
        blogPostRepository.save(createPost(title = "제목3", content = "React 시작하기", url = "https://test.com/3"))
        flushAndClear()

        val page = blogPostRepository.searchByKeyword("Spring", PageRequest.of(0, 10))

        assertEquals(2, page.totalElements)
    }

    @Test
    fun `URL 존재 여부를 확인한다`() {
        blogPostRepository.save(createPost(url = "https://test.com/existing-post"))
        flushAndClear()

        assertTrue(blogPostRepository.existsByUrl("https://test.com/existing-post"))
        assertFalse(blogPostRepository.existsByUrl("https://test.com/non-existing"))
    }

    @Test
    fun `ID로 BlogPost를 BlogSource와 함께 조회한다`() {
        val post = blogPostRepository.save(createPost(title = "Fetch Join 테스트"))
        flushAndClear()

        val found = blogPostRepository.findByIdWithSource(post.id)

        assertNotNull(found)
        assertAll(
            { assertEquals("Fetch Join 테스트", found.title) },
            { assertEquals("카카오 기술블로그", found.blogSource.name) }
        )
    }

    @Test
    fun `존재하지 않는 ID로 findByIdWithSource 호출 시 null을 반환한다`() {
        val found = blogPostRepository.findByIdWithSource(999L)
        assertEquals(null, found)
    }

    @Test
    fun `소스를 삭제하면 연관된 글도 삭제된다`() {
        blogPostRepository.save(createPost(blogSource = source1, url = "https://test.com/cascade-1"))
        blogPostRepository.save(createPost(blogSource = source1, url = "https://test.com/cascade-2"))
        flushAndClear()

        val sourceToDelete = blogSourceRepository.findById(source1.id).get()
        blogSourceRepository.delete(sourceToDelete)
        flushAndClear()

        assertFalse(blogPostRepository.existsByUrl("https://test.com/cascade-1"))
        assertFalse(blogPostRepository.existsByUrl("https://test.com/cascade-2"))
    }
}
