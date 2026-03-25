package ai.practice.summarizer

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class GeminiSummarizer(
    @Value("\${gemini.api-key:}") private val apiKey: String,
    @Value("\${gemini.model:gemini-2.0-flash}") private val model: String
) : Summarizer {
    private val log = LoggerFactory.getLogger(javaClass)
    private val restClient = RestClient.create()

    override fun summarize(title: String, content: String): SummarizeResult? {
        if (content.isBlank() || apiKey.isBlank()) return null

        val truncated = content.take(3000)
        val prompt = """다음 기술블로그 글을 한국어로 2~3문장으로 요약해줘. 핵심 기술과 주요 내용만 간결하게 정리해줘.

제목: $title
내용: $truncated"""

        return try {
            val requestBody = mapOf(
                "contents" to listOf(
                    mapOf("parts" to listOf(mapOf("text" to prompt)))
                )
            )

            val response = restClient.post()
                .uri("https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent?key=${apiKey}")
                .header("Content-Type", "application/json")
                .body(requestBody)
                .retrieve()
                .body(GeminiResponse::class.java)

            val text = response?.candidates?.firstOrNull()
                ?.content?.parts?.firstOrNull()?.text ?: return null
            SummarizeResult(summary = text.trim(), tags = emptyList())
        } catch (e: Exception) {
            log.error("Gemini summarization failed for '$title': ${e.message}")
            null
        }
    }
}

data class GeminiResponse(
    val candidates: List<Candidate>? = null
)

data class Candidate(
    val content: Content? = null
)

data class Content(
    val parts: List<Part>? = null
)

data class Part(
    val text: String? = null
)
