package ai.practice.repository

import ai.practice.domain.BlogSource
import org.springframework.data.jpa.repository.JpaRepository

interface BlogSourceRepository : JpaRepository<BlogSource, Long>
