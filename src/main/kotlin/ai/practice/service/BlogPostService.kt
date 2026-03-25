package ai.practice.service

import ai.practice.dto.BlogPostDetailResponse
import ai.practice.dto.BlogPostListResponse
import ai.practice.dto.PageResponse
import ai.practice.exception.NotFoundException
import ai.practice.repository.BlogPostRepository
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class BlogPostService(
    private val blogPostRepository: BlogPostRepository
) {

    fun findAll(sourceId: Long?, tag: String?, pageable: Pageable): PageResponse<BlogPostListResponse> {
        val page = when {
            tag != null -> blogPostRepository.findByTag(tag, pageable)
            sourceId != null -> blogPostRepository.findByBlogSourceId(sourceId, pageable)
            else -> blogPostRepository.findAll(pageable)
        }
        return PageResponse.from(page) { BlogPostListResponse.from(it) }
    }

    fun findById(id: Long): BlogPostDetailResponse {
        val post = blogPostRepository.findByIdWithSource(id)
            ?: throw NotFoundException("BlogPost not found: id=$id")
        return BlogPostDetailResponse.from(post)
    }

    fun getAllTags(): List<String> {
        return blogPostRepository.findAllTags()
            .flatMap { it.split(",").map { t -> t.trim() } }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
    }

    fun search(keyword: String, pageable: Pageable): PageResponse<BlogPostListResponse> {
        val page = blogPostRepository.searchByKeyword(keyword, pageable)
        return PageResponse.from(page) { BlogPostListResponse.from(it) }
    }
}
