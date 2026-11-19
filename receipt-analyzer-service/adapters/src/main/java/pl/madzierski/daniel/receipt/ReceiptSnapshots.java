package pl.madzierski.daniel.receipt;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.madzierski.daniel.event.IntegrationEvent;
import pl.madzierski.daniel.readmodel.ReadModelSnapshotSource;
import pl.madzierski.daniel.receipt.event.ReceiptChanged;
import pl.madzierski.daniel.receipt.event.RevisionChanged;

import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
class ReceiptSnapshots implements ReadModelSnapshotSource {

    private final SqlReceiptRepository receiptRepository;
    private final SqlReceiptRevisionRepository revisionRepository;

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

    @Override
    public int order() {
        return 40;
    }

    @Override
    public Stream<IntegrationEvent> snapshots() {
        return Stream.concat(
            receiptRepository.findAll().stream().map(SqlReceipt::toReceipt).map(ReceiptSnapshots::changed),
            revisionRepository.findAll().stream().map(SqlReceiptRevision::toReceiptRevision).map(ReceiptSnapshots::changed));
    }
}
