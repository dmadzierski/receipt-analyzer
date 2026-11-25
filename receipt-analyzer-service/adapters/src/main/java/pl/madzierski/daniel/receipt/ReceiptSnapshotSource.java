package pl.madzierski.daniel.receipt;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.madzierski.daniel.event.IntegrationEvent;
import pl.madzierski.daniel.readmodel.ReadModelSnapshotSource;

import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
class ReceiptSnapshotSource implements ReadModelSnapshotSource {

    private final SqlReceiptRepository receiptRepository;
    private final SqlReceiptRevisionRepository revisionRepository;

    @Override
    public int order() {
        return 40;
    }

    @Override
    public Stream<IntegrationEvent> snapshots() {
        return Stream.concat(
            receiptRepository.findAll().stream().map(SqlReceipt::toReceipt).map(ReceiptEventMapper::changed),
            revisionRepository.findAll().stream().map(SqlReceiptRevision::toReceiptRevision).map(ReceiptEventMapper::changed));
    }
}
