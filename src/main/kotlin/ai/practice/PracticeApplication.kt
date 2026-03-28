package ai.practice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PracticeApplication

fun main(args: Array<String>) {
    System.setProperty("jdk.xml.maxGeneralEntitySizeLimit", "0")
    runApplication<PracticeApplication>(*args)
}
