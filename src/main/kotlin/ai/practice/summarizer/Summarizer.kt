package ai.practice.summarizer

interface Summarizer {
    fun summarize(title: String, content: String): String?
}
