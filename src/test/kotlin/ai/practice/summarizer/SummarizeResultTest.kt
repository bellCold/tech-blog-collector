package ai.practice.summarizer

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import kotlin.test.assertEquals

class SummarizeResultTest {

    @Test
    fun `SummarizeResult를 생성한다`() {
        val result = SummarizeResult(
            summary = "Spring Boot 요약",
            tags = listOf("백엔드", "DevOps")
        )

        assertAll(
            { assertEquals("Spring Boot 요약", result.summary) },
            { assertEquals(listOf("백엔드", "DevOps"), result.tags) }
        )
    }

    @Test
    fun `빈 태그 리스트로 생성할 수 있다`() {
        val result = SummarizeResult(summary = "요약", tags = emptyList())

        assertEquals(emptyList(), result.tags)
    }

    @Test
    fun `data class copy로 변경할 수 있다`() {
        val original = SummarizeResult(summary = "원래 요약", tags = listOf("백엔드"))
        val modified = original.copy(tags = listOf("프론트엔드", "AI/ML"))

        assertAll(
            { assertEquals("원래 요약", modified.summary) },
            { assertEquals(listOf("프론트엔드", "AI/ML"), modified.tags) }
        )
    }
}
