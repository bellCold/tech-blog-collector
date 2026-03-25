package ai.practice.controller

import ai.practice.dto.BlogPostDetailResponse
import ai.practice.dto.BlogPostListResponse
import ai.practice.dto.PageResponse
import ai.practice.service.BlogPostService
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/posts")
class BlogPostController(
    private val blogPostService: BlogPostService
) {

    @GetMapping
    fun findAll(
        @RequestParam(required = false) sourceId: Long?,
        @PageableDefault(size = 20, sort = ["publishedAt"], direction = org.springframework.data.domain.Sort.Direction.DESC) pageable: Pageable
    ): PageResponse<BlogPostListResponse> {
        return blogPostService.findAll(sourceId, pageable)
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): BlogPostDetailResponse {
        return blogPostService.findById(id)
    }

    @GetMapping("/search")
    fun search(
        @RequestParam keyword: String,
        @PageableDefault(size = 20, sort = ["publishedAt"], direction = org.springframework.data.domain.Sort.Direction.DESC) pageable: Pageable
    ): PageResponse<BlogPostListResponse> {
        return blogPostService.search(keyword, pageable)
    }
}
