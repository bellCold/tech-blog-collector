package ai.practice.dto

import ai.practice.domain.BlogSource
import ai.practice.domain.SourceType
import java.time.LocalDateTime

data class CreateBlogSourceRequest(
    val name: String,
    val url: String,
    val rssUrl: String? = null,
    val type: SourceType,
    val description: String? = null
)

data class UpdateBlogSourceRequest(
    val name: String? = null,
    val url: String? = null,
    val rssUrl: String? = null,
    val type: SourceType? = null,
    val description: String? = null
)

data class BlogSourceResponse(
    val id: Long,
    val name: String,
    val url: String,
    val rssUrl: String?,
    val type: SourceType,
    val description: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(source: BlogSource) = BlogSourceResponse(
            id = source.id,
            name = source.name,
            url = source.url,
            rssUrl = source.rssUrl,
            type = source.type,
            description = source.description,
            createdAt = source.createdAt,
            updatedAt = source.updatedAt
        )
    }
}
