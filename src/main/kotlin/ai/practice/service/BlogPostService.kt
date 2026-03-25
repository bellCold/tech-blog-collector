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

    fun findAll(sourceId: Long?, pageable: Pageable): PageResponse<BlogPostListResponse> {
        val page = if (sourceId != null) {
            blogPostRepository.findByBlogSourceId(sourceId, pageable)
        } else {
            blogPostRepository.findAll(pageable)
        }
        return PageResponse.from(page) { BlogPostListResponse.from(it) }
    }

    fun findById(id: Long): BlogPostDetailResponse {
        val post = blogPostRepository.findByIdWithSource(id)
            ?: throw NotFoundException("BlogPost not found: id=$id")
        return BlogPostDetailResponse.from(post)
    }

    fun search(keyword: String, pageable: Pageable): PageResponse<BlogPostListResponse> {
        val page = blogPostRepository.searchByKeyword(keyword, pageable)
        return PageResponse.from(page) { BlogPostListResponse.from(it) }
    }
}
