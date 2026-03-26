package ai.practice.config

import ai.practice.summarizer.NoOpSummarizer
import ai.practice.summarizer.OllamaSummarizer
import ai.practice.summarizer.Summarizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Configuration
class SummarizerConfig {
    @Bean
    @Primary
    @Profile("!prod")
    fun localSummarizer(ollama: OllamaSummarizer): Summarizer = ollama

    @Bean
    @Primary
    @Profile("prod")
    fun prodSummarizer(noOp: NoOpSummarizer): Summarizer = noOp
}
