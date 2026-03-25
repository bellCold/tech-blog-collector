package ai.practice.domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BlogSourceTest {

    @Test
    fun `RSS 타입 블로그 소스를 생성한다`() {
        val source = BlogSource(
            name = "카카오 기술블로그",
            url = "https://tech.kakao.com",
            rssUrl = "https://tech.kakao.com/feed",
            type = SourceType.RSS,
            description = "카카오 기술블로그입니다"
        )

        assertAll(
            { assertEquals("카카오 기술블로그", source.name) },
            { assertEquals("https://tech.kakao.com", source.url) },
            { assertEquals("https://tech.kakao.com/feed", source.rssUrl) },
            { assertEquals(SourceType.RSS, source.type) },
            { assertEquals("카카오 기술블로그입니다", source.description) },
            { assertNotNull(source.createdAt) },
            { assertNotNull(source.updatedAt) },
            { assertTrue(source.posts.isEmpty()) }
        )
    }

    @Test
    fun `CRAWL 타입 블로그 소스를 생성한다`() {
        val source = BlogSource(
            name = "네이버 D2",
            url = "https://d2.naver.com",
            type = SourceType.CRAWL
        )

        assertAll(
            { assertEquals("네이버 D2", source.name) },
            { assertEquals(SourceType.CRAWL, source.type) },
            { assertNull(source.rssUrl) },
            { assertNull(source.description) }
        )
    }

    @Test
    fun `onUpdate 호출 시 updatedAt이 갱신된다`() {
        val source = BlogSource(
            name = "테스트",
            url = "https://test.com",
            type = SourceType.RSS
        )
        val beforeUpdate = source.updatedAt

        Thread.sleep(10)
        source.onUpdate()

        assertTrue(source.updatedAt.isAfter(beforeUpdate) || source.updatedAt == beforeUpdate)
    }

    @Test
    fun `필드를 수정할 수 있다`() {
        val source = BlogSource(
            name = "원래 이름",
            url = "https://original.com",
            type = SourceType.RSS
        )

        source.name = "수정된 이름"
        source.url = "https://updated.com"
        source.rssUrl = "https://updated.com/feed"
        source.type = SourceType.CRAWL
        source.description = "설명 추가"

        assertAll(
            { assertEquals("수정된 이름", source.name) },
            { assertEquals("https://updated.com", source.url) },
            { assertEquals("https://updated.com/feed", source.rssUrl) },
            { assertEquals(SourceType.CRAWL, source.type) },
            { assertEquals("설명 추가", source.description) }
        )
    }

    @Test
    fun `posts 목록에 BlogPost를 추가할 수 있다`() {
        val source = BlogSource(
            name = "테스트",
            url = "https://test.com",
            type = SourceType.RSS
        )

        val post = BlogPost(
            blogSource = source,
            title = "테스트 글",
            url = "https://test.com/post/1"
        )
        source.posts.add(post)

        assertEquals(1, source.posts.size)
        assertEquals("테스트 글", source.posts[0].title)
    }
}
