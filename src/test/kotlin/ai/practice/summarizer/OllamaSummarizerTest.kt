package ai.practice.summarizer

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.lang.reflect.Method
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OllamaSummarizerTest {

    private val summarizer = OllamaSummarizer(ollamaUrl = "http://localhost:11434", model = "gemma3:27b")

    private fun parseResult(text: String): SummarizeResult {
        val method: Method = OllamaSummarizer::class.java.getDeclaredMethod("parseResult", String::class.java)
        method.isAccessible = true
        return method.invoke(summarizer, text) as SummarizeResult
    }

    @Test
    fun `정상 형식의 응답을 파싱한다`() {
        val text = """
            [요약]
            Spring Boot는 자바 기반 웹 애플리케이션을 빠르게 개발할 수 있게 해주는 프레임워크이다.
            [태그]
            백엔드, DevOps
        """.trimIndent()

        val result = parseResult(text)

        assertAll(
            { assertTrue(result.summary.contains("Spring Boot")) },
            { assertEquals(listOf("백엔드", "DevOps"), result.tags) }
        )
    }

    @Test
    fun `태그가 VALID_TAGS에 없으면 필터링된다`() {
        val text = """
            [요약]
            요약 내용
            [태그]
            백엔드, 잘못된태그, AI/ML
        """.trimIndent()

        val result = parseResult(text)

        assertEquals(listOf("백엔드", "AI/ML"), result.tags)
    }

    @Test
    fun `태그 섹션이 없으면 빈 리스트를 반환한다`() {
        val text = """
            [요약]
            요약만 있는 응답
        """.trimIndent()

        val result = parseResult(text)

        // 태그 섹션이 없으면 요약 매칭도 실패하므로 첫 줄이 요약으로 사용됨
        assertEquals(emptyList<String>(), result.tags)
    }

    @Test
    fun `요약 섹션이 없으면 첫 줄을 요약으로 사용한다`() {
        val text = "이것은 첫 줄 요약입니다.\n나머지 내용"

        val result = parseResult(text)

        assertEquals("이것은 첫 줄 요약입니다.", result.summary)
    }

    @Test
    fun `VALID_TAGS에 모든 허용 태그가 포함되어 있다`() {
        val expected = setOf(
            "프론트엔드", "백엔드", "모바일", "AI/ML",
            "DevOps", "인프라", "보안", "데이터", "문화/조직", "QA/테스트"
        )

        assertEquals(expected, OllamaSummarizer.VALID_TAGS)
    }

    @Test
    fun `content가 빈 문자열이면 null을 반환한다`() {
        val result = summarizer.summarize("제목", "")

        assertEquals(null, result)
    }
}
