package pl.madzierski.daniel.receipt;

import jakarta.persistence.*;
import lombok.*;
import pl.madzierski.daniel.common.entity.VersionedEntity;
import pl.madzierski.daniel.receipt.model.ReceiptRevisionQuery;
import pl.madzierski.daniel.store.model.StoreQuery;
import pl.madzierski.daniel.user.model.UserQuery;
import pl.madzierski.daniel.wallet.model.WalletQuery;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = true)
@Entity
@Getter
@Setter(AccessLevel.PRIVATE)
@Table(name = "receipt")
class SqlReceipt extends VersionedEntity {

    private String name;
    private String description;
    @Column(name = "wallet_id")
    private UUID walletId;
    @Column(name = "store_id")
    private UUID storeId;
    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;
    @Column(name = "preferred_revision_id")
    private UUID preferredRevisionId;

    public static SqlReceipt fromReceipt(Receipt receipt) {
        SqlReceipt sqlReceipt = new SqlReceipt();
        sqlReceipt.setId(receipt.getId());
        sqlReceipt.setVersion(receipt.getVersion());
        sqlReceipt.setCreatedDate(receipt.getCreatedDate());
        sqlReceipt.setModifiedDate(receipt.getModifiedDate());
        sqlReceipt.setName(receipt.getName());
        sqlReceipt.setDescription(receipt.getDescription());
        sqlReceipt.setWalletId(receipt.getWallet() != null ? receipt.getWallet().id() : null);
        sqlReceipt.setStoreId(receipt.getStore() != null ? receipt.getStore().id() : null);
        sqlReceipt.setOwnerId(receipt.getOwner() != null ? receipt.getOwner().id() : null);
        sqlReceipt.setPreferredRevisionId(receipt.getPreferredRevision() != null ? receipt.getPreferredRevision().id() : null);
        return sqlReceipt;
    }

    public Receipt toReceipt() {
        return Receipt.restore(id, version, createdDate, modifiedDate, name, description,
            walletId != null ? new WalletQuery(walletId) : null,
            storeId != null ? new StoreQuery(storeId) : null,
            ownerId != null ? new UserQuery(ownerId) : null,
            preferredRevisionId != null ? new ReceiptRevisionQuery(preferredRevisionId) : null);
    }
}