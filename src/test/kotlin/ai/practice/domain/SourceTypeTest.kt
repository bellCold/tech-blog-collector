package ai.practice.domain

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class SourceTypeTest {

    @Test
    fun `SourceType은 RSS와 CRAWL 두 가지 값을 가진다`() {
        val values = SourceType.entries
        assertEquals(2, values.size)
        assertEquals(SourceType.RSS, values[0])
        assertEquals(SourceType.CRAWL, values[1])
    }

    @Test
    fun `문자열로부터 SourceType을 변환할 수 있다`() {
        assertEquals(SourceType.RSS, SourceType.valueOf("RSS"))
        assertEquals(SourceType.CRAWL, SourceType.valueOf("CRAWL"))
    }
}
