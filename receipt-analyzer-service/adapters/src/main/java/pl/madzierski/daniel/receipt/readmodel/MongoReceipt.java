package pl.madzierski.daniel.receipt.readmodel;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import pl.madzierski.daniel.receipt.event.ReceiptChanged;
import pl.madzierski.daniel.receipt.model.ReceiptDto;

import java.time.LocalDateTime;
import java.util.UUID;

@Document("receipts")
record MongoReceipt(@Id UUID id, long version, UUID ownerId, @Indexed UUID walletId, UUID storeId, String name, String description,
                    LocalDateTime createdDate, LocalDateTime modifiedDate, UUID preferredRevisionId) {

    static MongoReceipt fromEvent(ReceiptChanged event) {
        return new MongoReceipt(event.id(), event.version(), event.ownerId(), event.walletId(), event.storeId(), event.name(),
            event.description(), event.createdDate(), event.modifiedDate(), event.preferredRevisionId());
    }

    ReceiptDto toDto() {
        return new ReceiptDto(id, version, createdDate, modifiedDate, name, description, walletId, storeId);
    }
}
