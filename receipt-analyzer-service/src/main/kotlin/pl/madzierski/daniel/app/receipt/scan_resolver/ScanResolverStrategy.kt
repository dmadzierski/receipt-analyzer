package pl.madzierski.daniel.app.receipt.scan_resolver

import pl.madzierski.daniel.app.receipt.revision.model.ReceiptRevisionResolveData

interface ReceiptResolverStrategyTypeStrategy {

    fun strategy(): ReceiptResolverStrategyType

    fun execute(
        filePath: String
    ): ReceiptRevisionResolveData

}