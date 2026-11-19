package pl.madzierski.daniel.receipt;

import lombok.*;
import pl.madzierski.daniel.exception.AppIllegalStateException;
import pl.madzierski.daniel.exception.AppIllegalStateExceptionMessages;
import pl.madzierski.daniel.receipt.model.ReceiptRevisionQuery;
import pl.madzierski.daniel.store.model.StoreQuery;
import pl.madzierski.daniel.user.model.UserQuery;
import pl.madzierski.daniel.wallet.model.WalletQuery;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(exclude = "wallet")
class Receipt {

    private UUID id;
    private Long version;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String name;
    private String description;
    private WalletQuery wallet;
    private StoreQuery store;
    private UserQuery owner;
    private ReceiptRevisionQuery preferredRevision;

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

    static Receipt restore(UUID id, Long version, LocalDateTime createdDate, LocalDateTime modifiedDate, String name,
                           String description, WalletQuery wallet, StoreQuery store, UserQuery owner,
                           ReceiptRevisionQuery preferredRevision) {
        return builder()
            .id(id)
            .version(version)
            .createdDate(createdDate)
            .modifiedDate(modifiedDate)
            .name(name)
            .description(description)
            .wallet(wallet)
            .store(store)
            .owner(owner)
            .preferredRevision(preferredRevision)
            .build();
    }

    void updateData(String trim, String description, StoreQuery storeQuery) {
        this.name = trim;
        this.description = description;
        this.store = storeQuery;
    }

    boolean isPreferred(ReceiptRevision revision) {
        return preferredRevision != null && preferredRevision.id().equals(revision.getId());
    }

    void choosePreferredRevision(ReceiptRevision revision) {
        ensureOwnRevision(revision);
        this.preferredRevision = new ReceiptRevisionQuery(revision.getId());
    }

    void unmarkPreferredRevision(ReceiptRevision revision) {
        ensureOwnRevision(revision);
        if (isPreferred(revision)) this.preferredRevision = null;
    }

    private void ensureOwnRevision(ReceiptRevision revision) {
        if (revision.getReceipt() == null || !revision.getReceipt().id().equals(id))
            throw new AppIllegalStateException(AppIllegalStateExceptionMessages.REVISION_NOT_OF_RECEIPT);
    }
}
