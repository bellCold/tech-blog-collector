package ai.practice.service

import ai.practice.domain.BlogSource
import ai.practice.dto.BlogSourceResponse
import ai.practice.dto.CreateBlogSourceRequest
import ai.practice.dto.UpdateBlogSourceRequest
import ai.practice.exception.NotFoundException
import ai.practice.repository.BlogSourceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class BlogSourceService(
    private val blogSourceRepository: BlogSourceRepository
) {

    @Transactional
    fun create(request: CreateBlogSourceRequest): BlogSourceResponse {
        val source = BlogSource(
            name = request.name,
            url = request.url,
            rssUrl = request.rssUrl,
            type = request.type,
            description = request.description
        )
        return BlogSourceResponse.from(blogSourceRepository.save(source))
    }

    fun findAll(): List<BlogSourceResponse> {
        return blogSourceRepository.findAll().map { BlogSourceResponse.from(it) }
    }

    fun findById(id: Long): BlogSourceResponse {
        val source = blogSourceRepository.findById(id)
            .orElseThrow { NotFoundException("BlogSource not found: id=$id") }
        return BlogSourceResponse.from(source)
    }

    @Transactional
    fun update(id: Long, request: UpdateBlogSourceRequest): BlogSourceResponse {
        val source = blogSourceRepository.findById(id)
            .orElseThrow { NotFoundException("BlogSource not found: id=$id") }

        request.name?.let { source.name = it }
        request.url?.let { source.url = it }
        request.rssUrl?.let { source.rssUrl = it }
        request.type?.let { source.type = it }
        request.description?.let { source.description = it }

        return BlogSourceResponse.from(source)
    }

    @Transactional
    fun delete(id: Long) {
        if (!blogSourceRepository.existsById(id)) {
            throw NotFoundException("BlogSource not found: id=$id")
        }
        blogSourceRepository.deleteById(id)
    }
}
