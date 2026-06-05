package pl.madzierski.daniel.app.receipt.scan_resolver.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import pl.madzierski.daniel.app.receipt.revision.model.ReceiptRevisionResolveData
import pl.madzierski.daniel.app.receipt.scan_resolver.ReceiptResolverStrategyType
import pl.madzierski.daniel.app.receipt.scan_resolver.ReceiptResolverStrategyTypeStrategy

@Service
class ReceiptResolverService @Autowired constructor(
    strategies: List<ReceiptResolverStrategyTypeStrategy>
) {
    private val strategyMap = strategies.associateBy { it.strategy() }
    fun resolve(
        filePath: String,
        strategyType: ReceiptResolverStrategyType
    ): ReceiptRevisionResolveData {
        val strategy =
            strategyMap[strategyType] ?: throw IllegalArgumentException("Strategy not found: $strategyType")
        return strategy.execute(filePath)
    }
}