package pl.madzierski.daniel.wallet.readmodel;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import pl.madzierski.daniel.wallet.event.WalletChanged;
import pl.madzierski.daniel.wallet.model.WalletDto;

import java.time.LocalDateTime;
import java.util.UUID;

@Document("wallets")
record MongoWallet(@Id UUID id, @Indexed UUID ownerId, String name, LocalDateTime createdDate) {

    static MongoWallet fromEvent(WalletChanged event) {
        return new MongoWallet(event.id(), event.ownerId(), event.name(), event.createdDate());
    }

    WalletDto toDto() {
        return new WalletDto(id, name, createdDate);
    }
}
