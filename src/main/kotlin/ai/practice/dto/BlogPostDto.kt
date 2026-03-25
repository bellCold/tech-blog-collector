package ai.practice.dto

import ai.practice.domain.BlogPost
import java.time.LocalDateTime

data class BlogPostListResponse(
    val id: Long,
    val title: String,
    val summary: String?,
    val url: String,
    val author: String?,
    val sourceName: String,
    val tags: List<String>,
    val publishedAt: LocalDateTime?
) {
    companion object {
        fun from(post: BlogPost) = BlogPostListResponse(
            id = post.id,
            title = post.title,
            summary = post.summary ?: post.content?.take(200),
            url = post.url,
            author = post.author,
            sourceName = post.blogSource.name,
            tags = post.tags?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList(),
            publishedAt = post.publishedAt
        )
    }
}

data class BlogPostDetailResponse(
    val id: Long,
    val title: String,
    val content: String?,
    val summary: String?,
    val url: String,
    val author: String?,
    val source: BlogSourceResponse,
    val tags: List<String>,
    val publishedAt: LocalDateTime?,
    val collectedAt: LocalDateTime
) {
    companion object {
        fun from(post: BlogPost) = BlogPostDetailResponse(
            id = post.id,
            title = post.title,
            content = post.content,
            summary = post.summary,
            url = post.url,
            author = post.author,
            source = BlogSourceResponse.from(post.blogSource),
            tags = post.tags?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList(),
            publishedAt = post.publishedAt,
            collectedAt = post.collectedAt
        )
    }
}
