package pl.madzierski.daniel.app.receipt.revision

import org.springframework.stereotype.Service
import pl.madzierski.daniel.app.receipt.ReceiptProvider
import pl.madzierski.daniel.app.receipt.revision.item.ItemEntity
import pl.madzierski.daniel.app.receipt.revision.item.ItemService
import pl.madzierski.daniel.app.receipt.revision.model.AddRevisionRequest
import pl.madzierski.daniel.app.receipt.revision.model.AddRevisionResponse
import pl.madzierski.daniel.exception.AppRuntimeException
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages

@Service
class ReceiptRevisionService(
    val receiptRevisionRepository: ReceiptRevisionRepository,
    val receiptProvider: ReceiptProvider,
    val itemService: ItemService,
) {

    fun saveDefaultReceiptRevision(receiptRevision: ReceiptRevisionEntity): ReceiptRevisionEntity =
        this.save(receiptRevision)

    private fun save(receiptRevision: ReceiptRevisionEntity): ReceiptRevisionEntity {
        return receiptRevisionRepository.save(receiptRevision)
    }

    fun addRevision(revisionRequest: AddRevisionRequest): AddRevisionResponse {
        val receiptEntity = receiptProvider.findById(revisionRequest.receiptId)
            .orElseThrow { AppRuntimeException(AppRuntimeExceptionMessages.RECEIPT_NOT_FOUND) }

        val receiptRevisionEntity = ReceiptRevisionEntity(
            "1.0",
            ScanResolver.USER,
            revisionRequest.brand,
            receiptEntity,
            mutableSetOf(),
            mutableSetOf(),
            revisionRequest.totalPrice,
            revisionRequest.payingDate,
            revisionRequest.address,
            false,
            false
        )

        val revisionEntity = receiptRevisionRepository.save(receiptRevisionEntity)

        revisionEntity.items = revisionRequest.items?.map { item ->
            val parentItem = item.originalItemId?.let { itemId ->
                itemService.findById(itemId).orElse(null)
            }
            ItemEntity(
                revisionEntity,
                item.name,
                item.ptu,
                item.amount,
                item.unitPrice,
                item.discount,
                item.totalPrice,
                item.position,
                parentItem
            )
        }?.let { items ->
            itemService.saveAll(items).toMutableSet()
        } ?: mutableSetOf()

        return AddRevisionResponse.addRevisionMapper(receiptRevisionEntity)
    }
}