package pl.madzierski.daniel.receipt;

import lombok.*;
import pl.madzierski.daniel.file_group.model.FileGroupQuery;
import pl.madzierski.daniel.store.model.StoreQuery;
import pl.madzierski.daniel.user.model.UserQuery;
import pl.madzierski.daniel.wallet.model.WalletQuery;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(exclude = {"wallet", "receiptRevisions", "fileGroups"})
class Receipt {

    private UUID id;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String name;
    private String description;
    private WalletQuery wallet;
    private StoreQuery store;
    private UserQuery owner;
    private final Set<FileGroupQuery> fileGroups = new HashSet<>();
    private final Set<ReceiptRevision> receiptRevisions = new HashSet<>();

    static Receipt create(String name, String description, WalletQuery wallet, StoreQuery store,
                           UserQuery owner) {
        return builder()
            .name(name != null && !name.isBlank() ? name : LocalDateTime.now().toString())
            .description(description)
            .wallet(wallet)
            .store(store)
            .owner(owner)
            .build();
    }

    static Receipt restore(UUID id, LocalDateTime createdDate, LocalDateTime modifiedDate, String name,
                           String description, WalletQuery wallet, StoreQuery store, UserQuery owner) {
        return builder()
            .id(id)
            .createdDate(createdDate)
            .modifiedDate(modifiedDate)
            .name(name)
            .description(description)
            .wallet(wallet)
            .store(store)
            .owner(owner)
            .build();
    }

    void updateData(String trim, String description, StoreQuery storeQuery) {
        this.name = trim;
        this.description = description;
        this.store = storeQuery;
    }
}