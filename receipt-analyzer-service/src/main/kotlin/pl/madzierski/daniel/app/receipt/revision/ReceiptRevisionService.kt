package pl.madzierski.daniel.app.receipt.revision

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pl.madzierski.daniel.app.receipt.ReceiptProvider
import pl.madzierski.daniel.app.receipt.revision.item.ItemEntity
import pl.madzierski.daniel.app.receipt.revision.item.ItemProvider
import pl.madzierski.daniel.app.receipt.revision.model.*
import pl.madzierski.daniel.app.receipt.scan_resolver.ReceiptResolverStrategyType
import pl.madzierski.daniel.exception.AppRuntimeException
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages

@Service
class ReceiptRevisionService(
    val revisionRepository: ReceiptRevisionRepository,
    val receiptProvider: ReceiptProvider,
    val itemProvider: ItemProvider,
) {

    fun saveReceiptRevision(receiptRevision: ReceiptRevisionEntity): ReceiptRevisionEntity = this.save(receiptRevision)

    private fun save(receiptRevision: ReceiptRevisionEntity): ReceiptRevisionEntity {
        return revisionRepository.save(receiptRevision)
    }

    fun addRevision(revisionRequest: AddRevisionRequest): AddRevisionResponse {
        val receiptEntity = receiptProvider.findById(revisionRequest.receiptId)
            .orElseThrow { AppRuntimeException(AppRuntimeExceptionMessages.RECEIPT_NOT_FOUND) }

        val receiptRevisionEntity = ReceiptRevisionEntity(
            "",
            "1.0",
            ReceiptResolverStrategyType.USER,
            revisionRequest.brand,
            receiptEntity,
            mutableSetOf(),
            revisionRequest.totalPrice,
            revisionRequest.payingDate,
            revisionRequest.address,
            false,
            false
        )

        val revisionEntity = revisionRepository.save(receiptRevisionEntity)

        revisionEntity.addItems(revisionRequest.items?.map { item ->
            val parentItem = item.originalItemId?.let { itemId ->
                itemProvider.findById(itemId).orElse(null)
            }
            ItemEntity(
                null,
                item.name,
                item.amount,
                item.unitPrice,
                item.discount,
                item.totalPrice,
                item.position,
                parentItem
            )
        }?.let { items ->
            itemProvider.saveAll(items).toMutableSet()
        } ?: mutableSetOf())

        return AddRevisionResponse.addRevisionMapper(receiptRevisionEntity)
    }

    @Transactional
    fun createRevisionCopy(revisionId: String): RevisionCopyResponse {
        val revision = getRevisionById(revisionId)
        val revisionCopy = ReceiptRevisionEntity(
            name = revision.name + "(copy)",
            revision = "",
            resolver = ReceiptResolverStrategyType.USER,
            brand = revision.brand,
            receipt = revision.receipt,
            items = mutableSetOf(),
            totalPrice = revision.totalPrice,
            payingDate = revision.payingDate,
            address = revision.address,
            isPreferredRevision = false,
            isCorrect = revision.isCorrect,
            parentReceiptRevision = revision,
            childReceiptRevisions = mutableSetOf(),
        )
        revisionCopy.addItems(revision.items.map(::createItemCopy))

        val savedRevisionCopy = revisionRepository.save(revisionCopy)
        return RevisionCopyResponse(savedRevisionCopy.id)
    }

    private fun createItemCopy(item: ItemEntity): ItemEntity {
        return ItemEntity(
            receiptRevision = null,
            name = item.name,
            amount = item.amount,
            unitPrice = item.unitPrice,
            discount = item.discount,
            totalPrice = item.totalPrice,
            position = item.position,
            parentItem = item,
            childItems = mutableSetOf(),
        )
    }

    fun getRevision(revisionId: String): GetRevisionResponse? {
        return GetRevisionResponse.revisionMapper(
            revisionRepository.findById(revisionId)
                .orElseThrow { AppRuntimeException(AppRuntimeExceptionMessages.REVISION_NOT_FOUND) })
    }

    @Transactional
    fun updateRevision(revisionId: String, updatedRevision: UpdateRevisionRequest): UpdateRevisionResponse? {
        val currentRevision = getRevisionById(revisionId)
        require(currentRevision.resolver == ReceiptResolverStrategyType.USER)

        currentRevision.apply {
            updatedRevision.brand?.let { brand = it }
            updatedRevision.totalPrice?.let { totalPrice = it }
            updatedRevision.payingDate?.let { payingDate = it }
            updatedRevision.address?.let { address = it }
            updatedRevision.isPreferredRevision?.let { isPreferredRevision = it }
            updatedRevision.isCorrect?.let { isCorrect = it }
            updatedRevision.items?.let { incomingItems ->
                val incomingIds = incomingItems.mapNotNull { it.id }.toSet()
                items.removeIf { existingItem ->
                    existingItem.id !in incomingIds
                }
                incomingItems.forEach { incomingItem ->
                    if (incomingItem.id?.isNotBlank() ?: false) {
                        val existingItem = items.find { it.id == incomingItem.id }
                        existingItem?.apply {
                            incomingItem.name?.let { name = it }
                            incomingItem.amount?.let { amount = it }
                            incomingItem.unitPrice?.let { unitPrice = it }
                            incomingItem.totalPrice?.let { totalPrice = it }
                            incomingItem.position?.let { position = it }
                        }
                    } else {
                        val newItem = ItemEntity(
                            name = incomingItem.name,
                            amount = incomingItem.amount,
                            unitPrice = incomingItem.unitPrice,
                            totalPrice = incomingItem.totalPrice,
                            position = incomingItem.position,
                            receiptRevision = this@apply,
                            discount = 0.0,
                            parentItem = null,
                            childItems = mutableSetOf()
                        )
                        items.add(itemProvider.save(newItem))
                    }
                }
            }
        }
        val savedRevision = revisionRepository.save(currentRevision)
        return UpdateRevisionResponse.revisionMapper(savedRevision)
    }

    private fun getRevisionById(revisionId: String): ReceiptRevisionEntity {
        return revisionRepository.findById(revisionId)
            .orElseThrow { AppRuntimeException(AppRuntimeExceptionMessages.REVISION_NOT_FOUND) }
    }

}