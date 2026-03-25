package ai.practice.summarizer

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Primary
@Component
class OllamaSummarizer(
    @Value("\${ollama.url:http://localhost:11434}") private val ollamaUrl: String,
    @Value("\${ollama.model:gemma3:4b}") private val model: String
) : Summarizer {
    private val log = LoggerFactory.getLogger(javaClass)
    private val restClient = RestClient.create()

    override fun summarize(title: String, content: String): String? {
        if (content.isBlank()) return null

        val truncated = content.take(3000)
        val prompt = """다음 기술블로그 글을 한국어로 2~3문장으로 요약해줘. 핵심 기술과 주요 내용만 간결하게 정리해줘.

제목: $title
내용: $truncated"""

        return try {
            val requestBody = mapOf(
                "model" to model,
                "prompt" to prompt,
                "stream" to false
            )

            val response = restClient.post()
                .uri("$ollamaUrl/api/generate")
                .header("Content-Type", "application/json")
                .body(requestBody)
                .retrieve()
                .body(OllamaResponse::class.java)

            response?.response?.trim()
        } catch (e: Exception) {
            log.error("Ollama summarization failed for '$title': ${e.message}")
            null
        }
    }
}

data class OllamaResponse(
    val response: String? = null
)
