package pl.madzierski.daniel.receipt.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import pl.madzierski.daniel.store.model.StoreQuery;
import pl.madzierski.daniel.wallet.model.WalletQuery;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ReceiptDto {
    private String id;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String name;
    private String description;
    private WalletQuery wallet;
    private StoreQuery store;

    public ReceiptDto(String id, LocalDateTime createdDate, LocalDateTime modifiedDate, String name, String description, WalletQuery wallet) {
        this(id, createdDate, modifiedDate, name, description, wallet, null);
    }
}
