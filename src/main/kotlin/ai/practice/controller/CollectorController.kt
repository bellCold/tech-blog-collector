package ai.practice.controller

import ai.practice.collector.CollectorScheduler
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/collector")
class CollectorController(
    private val collectorScheduler: CollectorScheduler
) {

    @PostMapping("/run")
    @ResponseStatus(HttpStatus.OK)
    fun runAll(): Map<String, String> {
        collectorScheduler.collectAll()
        return mapOf("message" to "Collection triggered")
    }

    @PostMapping("/run/{sourceId}")
    @ResponseStatus(HttpStatus.OK)
    fun runBySource(@PathVariable sourceId: Long): Map<String, String> {
        collectorScheduler.collectBySourceId(sourceId)
        return mapOf("message" to "Collection triggered for source $sourceId")
    }
}
