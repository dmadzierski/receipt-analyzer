package pl.madzierski.daniel

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ReceiptAnalyzerServiceApplication

fun main(args: Array<String>) {
    runApplication<ReceiptAnalyzerServiceApplication>(*args)
}
