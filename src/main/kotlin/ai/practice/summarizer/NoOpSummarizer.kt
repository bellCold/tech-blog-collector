package ai.practice.summarizer

import org.springframework.stereotype.Component

@Component
class NoOpSummarizer : Summarizer {
    override fun summarize(title: String, content: String): SummarizeResult? = null
}
