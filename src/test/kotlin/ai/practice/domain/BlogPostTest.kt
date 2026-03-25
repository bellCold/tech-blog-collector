package ai.practice.domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame

class BlogPostTest {

    private fun createSource() = BlogSource(
        name = "테스트 블로그",
        url = "https://test.com",
        type = SourceType.RSS
    )

    @Test
    fun `필수 필드만으로 BlogPost를 생성한다`() {
        val source = createSource()
        val post = BlogPost(
            blogSource = source,
            title = "Spring Boot 가이드",
            url = "https://test.com/spring-boot"
        )

        assertAll(
            { assertSame(source, post.blogSource) },
            { assertEquals("Spring Boot 가이드", post.title) },
            { assertEquals("https://test.com/spring-boot", post.url) },
            { assertNull(post.content) },
            { assertNull(post.summary) },
            { assertNull(post.author) },
            { assertNull(post.publishedAt) },
            { assertNotNull(post.collectedAt) },
            { assertNotNull(post.createdAt) }
        )
    }

    @Test
    fun `모든 필드를 포함하여 BlogPost를 생성한다`() {
        val source = createSource()
        val publishedAt = LocalDateTime.of(2026, 3, 20, 10, 0)

        val post = BlogPost(
            blogSource = source,
            title = "Kotlin 코루틴 가이드",
            content = "코루틴은 비동기 프로그래밍을 위한...",
            summary = "Kotlin 코루틴에 대한 종합 가이드",
            url = "https://test.com/kotlin-coroutine",
            author = "홍길동",
            publishedAt = publishedAt
        )

        assertAll(
            { assertEquals("Kotlin 코루틴 가이드", post.title) },
            { assertEquals("코루틴은 비동기 프로그래밍을 위한...", post.content) },
            { assertEquals("Kotlin 코루틴에 대한 종합 가이드", post.summary) },
            { assertEquals("https://test.com/kotlin-coroutine", post.url) },
            { assertEquals("홍길동", post.author) },
            { assertEquals(publishedAt, post.publishedAt) }
        )
    }

    @Test
    fun `title과 content를 수정할 수 있다`() {
        val post = BlogPost(
            blogSource = createSource(),
            title = "원래 제목",
            url = "https://test.com/post"
        )

        post.title = "수정된 제목"
        post.content = "새로운 본문"
        post.summary = "새로운 요약"
        post.author = "작성자"

        assertAll(
            { assertEquals("수정된 제목", post.title) },
            { assertEquals("새로운 본문", post.content) },
            { assertEquals("새로운 요약", post.summary) },
            { assertEquals("작성자", post.author) }
        )
    }

    @Test
    fun `tags 필드를 설정하고 조회할 수 있다`() {
        val post = BlogPost(
            blogSource = createSource(),
            title = "태그 테스트",
            url = "https://test.com/tags",
            tags = "백엔드,DevOps"
        )

        assertEquals("백엔드,DevOps", post.tags)
    }

    @Test
    fun `tags 기본값은 null이다`() {
        val post = BlogPost(
            blogSource = createSource(),
            title = "태그 없음",
            url = "https://test.com/no-tags"
        )

        assertNull(post.tags)
    }

    @Test
    fun `tags를 수정할 수 있다`() {
        val post = BlogPost(
            blogSource = createSource(),
            title = "태그 수정",
            url = "https://test.com/update-tags"
        )

        post.tags = "프론트엔드,AI/ML"

        assertEquals("프론트엔드,AI/ML", post.tags)
    }
}
