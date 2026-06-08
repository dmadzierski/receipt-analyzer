package pl.madzierski.daniel.app.receipt.scan_resolver.impl

import org.springframework.stereotype.Service
import pl.madzierski.daniel.app.receipt.revision.model.ReceiptRevisionResolveData
import pl.madzierski.daniel.app.receipt.scan_resolver.ReceiptResolverStrategyType
import pl.madzierski.daniel.app.receipt.scan_resolver.ReceiptResolverStrategyTypeStrategy

@Service
class UserResolver: ReceiptResolverStrategyTypeStrategy {
    override fun strategy(): ReceiptResolverStrategyType = ReceiptResolverStrategyType.USER

    override fun execute(filePath: String): ReceiptRevisionResolveData {
        return ReceiptRevisionResolveData(null, null, null, null)
    }
}