package ai.practice.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "blog_post",
    indexes = [
        Index(name = "idx_published_at", columnList = "publishedAt"),
        Index(name = "idx_blog_source_id", columnList = "blog_source_id")
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "uk_post_url", columnNames = ["url"])
    ]
)
class BlogPost(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_source_id", nullable = false)
    val blogSource: BlogSource,

    @Column(nullable = false, length = 500)
    var title: String,

    @Column(columnDefinition = "TEXT")
    var content: String? = null,

    @Column(length = 1000)
    var summary: String? = null,

    @Column(nullable = false, length = 1000)
    val url: String,

    @Column(length = 100)
    var author: String? = null,

    var publishedAt: LocalDateTime? = null,

    @Column(nullable = false)
    val collectedAt: LocalDateTime = LocalDateTime.now(),

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
