package ai.practice.repository

import ai.practice.domain.BlogSource
import ai.practice.domain.SourceType
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@DataJpaTest
class BlogSourceRepositoryTest @Autowired constructor(
    private val blogSourceRepository: BlogSourceRepository,
    private val entityManager: EntityManager
) {

    private fun createSource(
        name: String = "테스트 블로그",
        url: String = "https://test.com",
        type: SourceType = SourceType.RSS,
        rssUrl: String? = "https://test.com/feed",
        description: String? = null
    ): BlogSource = BlogSource(
        name = name,
        url = url,
        type = type,
        rssUrl = rssUrl,
        description = description
    )

    private fun flushAndClear() {
        entityManager.flush()
        entityManager.clear()
    }

    @Test
    fun `BlogSource를 저장하고 조회한다`() {
        val source = createSource(description = "테스트 설명")
        val saved = blogSourceRepository.save(source)
        flushAndClear()

        val found = blogSourceRepository.findById(saved.id).orElse(null)

        assertNotNull(found)
        assertAll(
            { assertEquals("테스트 블로그", found.name) },
            { assertEquals("https://test.com", found.url) },
            { assertEquals("https://test.com/feed", found.rssUrl) },
            { assertEquals(SourceType.RSS, found.type) },
            { assertEquals("테스트 설명", found.description) },
            { assertNotNull(found.createdAt) },
            { assertNotNull(found.updatedAt) }
        )
    }

    @Test
    fun `모든 BlogSource를 조회한다`() {
        blogSourceRepository.save(createSource(name = "블로그1", url = "https://blog1.com"))
        blogSourceRepository.save(createSource(name = "블로그2", url = "https://blog2.com"))
        flushAndClear()

        val sources = blogSourceRepository.findAll()

        assertEquals(2, sources.size)
    }

    @Test
    fun `BlogSource를 수정한다`() {
        val source = blogSourceRepository.save(createSource())
        flushAndClear()

        val found = blogSourceRepository.findById(source.id).get()
        found.name = "수정된 이름"
        found.description = "수정된 설명"
        flushAndClear()

        val updated = blogSourceRepository.findById(source.id).get()
        assertAll(
            { assertEquals("수정된 이름", updated.name) },
            { assertEquals("수정된 설명", updated.description) }
        )
    }

    @Test
    fun `BlogSource를 삭제한다`() {
        val source = blogSourceRepository.save(createSource())
        entityManager.flush()

        blogSourceRepository.delete(source)
        flushAndClear()

        val found = blogSourceRepository.findById(source.id).orElse(null)
        assertNull(found)
    }

    @Test
    fun `존재하지 않는 ID로 조회하면 빈 Optional을 반환한다`() {
        val found = blogSourceRepository.findById(999L)
        assertTrue(found.isEmpty)
    }
}
