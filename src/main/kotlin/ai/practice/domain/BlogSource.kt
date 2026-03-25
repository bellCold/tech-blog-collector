package ai.practice.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "blog_source")
class BlogSource(
    @Column(nullable = false)
    var name: String,

    @Column(nullable = false, unique = true)
    var url: String,

    var rssUrl: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    var type: SourceType,

    @Column(length = 500)
    var description: String? = null,

    var listSelector: String? = null,

    var titleSelector: String? = null,

    var contentSelector: String? = null,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @OneToMany(mappedBy = "blogSource", cascade = [CascadeType.ALL], orphanRemoval = true)
    val posts: MutableList<BlogPost> = mutableListOf()

    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }
}
