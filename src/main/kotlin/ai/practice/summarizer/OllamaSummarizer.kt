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
    @Value("\${ollama.model:gemma3:27b}") private val model: String
) : Summarizer {
    private val log = LoggerFactory.getLogger(javaClass)
    private val restClient = RestClient.create()

    companion object {
        val VALID_TAGS = setOf(
            "프론트엔드", "백엔드", "모바일", "AI/ML",
            "DevOps", "인프라", "보안", "데이터", "문화/조직", "QA/테스트"
        )
    }

    override fun summarize(title: String, content: String): SummarizeResult? {
        if (content.isBlank()) return null

        val truncated = content.take(3000)
        val prompt = """당신은 기술블로그 분석 전문가입니다. 아래 글을 읽고 요약과 카테고리를 분류하세요.

규칙:
- 요약: 한국어 2~3문장, 핵심 기술과 주요 내용만 간결하게
- 태그: 아래 목록에서 1~2개만 선택
- 반드시 아래 형식으로만 출력

태그 목록: 프론트엔드, 백엔드, 모바일, AI/ML, DevOps, 인프라, 보안, 데이터, 문화/조직, QA/테스트

제목: $title
내용: $truncated

[요약]
(여기에 요약)
[태그]
(여기에 태그를 쉼표로 구분)"""

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

            val text = response?.response?.trim() ?: return null
            parseResult(text)
        } catch (e: Exception) {
            log.error("Ollama summarization failed for '$title': ${e.message}")
            null
        }
    }

    private fun parseResult(text: String): SummarizeResult {
        val summaryMatch = Regex("""\[요약]\s*\n?([\s\S]*?)\[태그]""").find(text)
        val tagMatch = Regex("""\[태그]\s*\n?(.+)""").find(text)

        val summary = summaryMatch?.groupValues?.get(1)?.trim()
            ?: text.lines().first().trim()

        val tags = tagMatch?.groupValues?.get(1)
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it in VALID_TAGS }
            ?: emptyList()

        return SummarizeResult(summary = summary, tags = tags)
    }
}

data class OllamaResponse(
    val response: String? = null
)
