package pl.madzierski.daniel.app.receipt.scan_resolver.impl

import pl.madzierski.daniel.app.receipt.revision.model.ReceiptRevisionResolveData
import pl.madzierski.daniel.app.receipt.scan_resolver.ReceiptResolverStrategyType
import pl.madzierski.daniel.app.receipt.scan_resolver.ReceiptResolverStrategyTypeStrategy

class BiedronkaJsonResolver:ReceiptResolverStrategyTypeStrategy {
    override fun strategy(): ReceiptResolverStrategyType = ReceiptResolverStrategyType.BIEDRONKA_JSON

    override fun execute(filePath: String): ReceiptRevisionResolveData {
        TODO("Not yet implemented")
    }
}