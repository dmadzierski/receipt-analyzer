package pl.madzierski.daniel

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@EnableJpaAuditing
@SpringBootApplication
class ReceiptAnalyzerServiceApplication

fun main(args: Array<String>) {
    runApplication<ReceiptAnalyzerServiceApplication>(*args)
}
