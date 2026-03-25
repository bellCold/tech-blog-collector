package ai.practice.repository

import ai.practice.domain.BlogPost
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface BlogPostRepository : JpaRepository<BlogPost, Long> {

    fun findByBlogSourceId(sourceId: Long, pageable: Pageable): Page<BlogPost>

    @Query("""
        SELECT p FROM BlogPost p
        WHERE p.title LIKE %:keyword% OR p.content LIKE %:keyword%
    """)
    fun searchByKeyword(keyword: String, pageable: Pageable): Page<BlogPost>

    @Query("SELECT p FROM BlogPost p WHERE p.tags LIKE %:tag%")
    fun findByTag(tag: String, pageable: Pageable): Page<BlogPost>

    @Query("SELECT DISTINCT p.tags FROM BlogPost p WHERE p.tags IS NOT NULL")
    fun findAllTags(): List<String>

    fun existsByUrl(url: String): Boolean

    @Query("SELECT p FROM BlogPost p JOIN FETCH p.blogSource WHERE p.id = :id")
    fun findByIdWithSource(id: Long): BlogPost?
}
