package ai.practice.controller

import ai.practice.dto.BlogSourceResponse
import ai.practice.dto.CreateBlogSourceRequest
import ai.practice.dto.UpdateBlogSourceRequest
import ai.practice.service.BlogSourceService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/sources")
class BlogSourceController(
    private val blogSourceService: BlogSourceService
) {

    @PostMapping
    fun create(@RequestBody request: CreateBlogSourceRequest): ResponseEntity<BlogSourceResponse> {
        val response = blogSourceService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    fun findAll(): List<BlogSourceResponse> {
        return blogSourceService.findAll()
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): BlogSourceResponse {
        return blogSourceService.findById(id)
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody request: UpdateBlogSourceRequest
    ): BlogSourceResponse {
        return blogSourceService.update(id, request)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) {
        blogSourceService.delete(id)
    }
}
