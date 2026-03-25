package ai.practice.summarizer

data class SummarizeResult(
    val summary: String,
    val tags: List<String>
)

interface Summarizer {
    fun summarize(title: String, content: String): SummarizeResult?
}
