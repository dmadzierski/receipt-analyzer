package pl.madzierski.daniel.receipt;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pl.madzierski.daniel.receipt.event.ReceiptChanged;
import pl.madzierski.daniel.receipt.event.RevisionChanged;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class ReceiptEventMapper {

    static ReceiptChanged changed(Receipt receipt) {
        return new ReceiptChanged(receipt.getId(), receipt.getVersion(), receipt.getOwner() != null ? receipt.getOwner().id() : null,
            receipt.getWallet() != null ? receipt.getWallet().id() : null, receipt.getStore() != null ? receipt.getStore().id() : null,
            receipt.getName(), receipt.getDescription(), receipt.getCreatedDate(), receipt.getModifiedDate(),
            receipt.getPreferredRevision() != null ? receipt.getPreferredRevision().id() : null);
    }

    static RevisionChanged changed(ReceiptRevision revision) {
        return new RevisionChanged(revision.getId(), revision.getVersion(), revision.getReceipt() != null ? revision.getReceipt().id() : null,
            revision.getResolver(), revision.getCreatedDate(), revision.getTotalPrice(), revision.getPaymentDate(),
            revision.getIsCorrect(),
            revision.getParentReceiptRevision() != null ? revision.getParentReceiptRevision().id() : null,
            revision.getSourceFileGroup() != null ? revision.getSourceFileGroup().id() : null,
            revision.getItems().stream()
                .map(item -> new RevisionChanged.Item(item.getId(), item.getName(),
                    item.getNameDict() != null ? item.getNameDict().id() : null, item.getAmount(), item.getUnitPrice(),
                    item.getDiscount(), item.getTotalPrice(), item.getPosition(),
                    item.getParentItem() != null ? item.getParentItem().id() : null))
                .toList());
    }
}
