package pl.madzierski.daniel.app.receipt.revision

import org.springframework.stereotype.Service
import pl.madzierski.daniel.app.receipt.ReceiptProvider
import pl.madzierski.daniel.app.receipt.revision.item.ItemEntity
import pl.madzierski.daniel.app.receipt.revision.item.ItemProvider
import pl.madzierski.daniel.app.receipt.revision.model.AddRevisionRequest
import pl.madzierski.daniel.app.receipt.revision.model.AddRevisionResponse
import pl.madzierski.daniel.app.receipt.model.GetReceiptRevisionsResponse
import pl.madzierski.daniel.app.receipt.revision.model.RevisionCopyResponse
import pl.madzierski.daniel.exception.AppRuntimeException
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages

@Service
class ReceiptRevisionService(
    val revisionRepository: ReceiptRevisionRepository,
    val receiptProvider: ReceiptProvider,
    val itemProvider: ItemProvider,
) {

    fun saveDefaultReceiptRevision(receiptRevision: ReceiptRevisionEntity): ReceiptRevisionEntity =
        this.save(receiptRevision)

    private fun save(receiptRevision: ReceiptRevisionEntity): ReceiptRevisionEntity {
        return revisionRepository.save(receiptRevision)
    }

    fun addRevision(revisionRequest: AddRevisionRequest): AddRevisionResponse {
        val receiptEntity = receiptProvider.findById(revisionRequest.receiptId)
            .orElseThrow { AppRuntimeException(AppRuntimeExceptionMessages.RECEIPT_NOT_FOUND) }

        val receiptRevisionEntity = ReceiptRevisionEntity(
            "",
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

        val revisionEntity = revisionRepository.save(receiptRevisionEntity)

        revisionEntity.items = revisionRequest.items?.map { item ->
            val parentItem = item.originalItemId?.let { itemId ->
                itemProvider.findById(itemId).orElse(null)
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
            itemProvider.saveAll(items).toMutableSet()
        } ?: mutableSetOf()

        return AddRevisionResponse.addRevisionMapper(receiptRevisionEntity)
    }

    fun createRevisionCopy(revisionId: String): RevisionCopyResponse {
        val revision = revisionRepository.findById(revisionId)
            .orElseThrow { AppRuntimeException(AppRuntimeExceptionMessages.RECEIPT_NOT_FOUND) }

        val revisionCopy = revision.copy(
            items = mutableSetOf(),
            isPreferredRevision = false,
            receiptFiles = mutableSetOf(),
            resolver = ScanResolver.USER,
            childReceiptRevisions = mutableSetOf(),
        ).let {
            it.id = null
            revisionRepository.save(it)
        }

        revision.items.map { item ->
            item.copy(parentItem = item, receiptRevision = revisionCopy, childItems = mutableSetOf())
                .also { it.id = null }
        }.toSet().let { items -> itemProvider.saveAll(items) }

        return RevisionCopyResponse(revisionCopy.id)
    }

    fun getReceiptRevisions(receiptId: String): List<GetReceiptRevisionsResponse>? {
        return revisionRepository.findReceiptRevisionEntityByReceiptId(receiptId).map { revision ->
            GetReceiptRevisionsResponse(
                revision.id,
                revision.resolver,
                revision.createdDate,
                revision.brand,
                revision.totalPrice,
                revision.payingDate,
                revision.address,
                revision.isPreferredRevision,
                revision.isCorrect
            )
        }
    }
}