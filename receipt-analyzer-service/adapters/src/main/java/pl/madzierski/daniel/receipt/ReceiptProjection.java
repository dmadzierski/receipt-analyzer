package pl.madzierski.daniel.receipt;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.madzierski.daniel.event.IntegrationEvent;
import pl.madzierski.daniel.readmodel.Projection;
import pl.madzierski.daniel.receipt.event.ItemProductsReassigned;
import pl.madzierski.daniel.receipt.event.ReceiptChanged;
import pl.madzierski.daniel.receipt.event.ReceiptDeleted;
import pl.madzierski.daniel.receipt.event.RevisionChanged;
import pl.madzierski.daniel.receipt.event.RevisionDeleted;

@Component
@RequiredArgsConstructor
class ReceiptProjection implements Projection {

    private final MongoReceiptRepository receiptRepository;
    private final MongoRevisionRepository revisionRepository;

    @Override
    public void apply(IntegrationEvent event) {
        switch (event) {
            case ReceiptChanged changed -> receiptRepository.save(MongoReceipt.fromEvent(changed));
            case ReceiptDeleted deleted -> {
                receiptRepository.deleteById(deleted.id());
                revisionRepository.deleteAllByReceiptId(deleted.id());
            }
            case RevisionChanged changed -> revisionRepository.save(MongoRevision.fromEvent(changed));
            case RevisionDeleted deleted -> revisionRepository.deleteById(deleted.id());
            case ItemProductsReassigned reassigned -> revisionRepository.reassignItemProducts(reassigned.fromProductIds(), reassigned.id());
            default -> {
            }
        }
    }

    @Override
    public void clear() {
        revisionRepository.deleteAll();
        receiptRepository.deleteAll();
    }
}
