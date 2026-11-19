package pl.madzierski.daniel.receipt.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import pl.madzierski.daniel.store.model.StoreQuery;
import pl.madzierski.daniel.wallet.model.WalletQuery;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ReceiptDto {
    private UUID id;
    private long version;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String name;
    private String description;
    private WalletQuery wallet;
    private StoreQuery store;
}
