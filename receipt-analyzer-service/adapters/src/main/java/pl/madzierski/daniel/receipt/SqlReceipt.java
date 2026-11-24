package pl.madzierski.daniel.receipt;

import jakarta.persistence.*;
import lombok.*;
import pl.madzierski.daniel.common.entity.VersionedEntity;
import pl.madzierski.daniel.receipt.model.ReceiptRevisionQuery;
import pl.madzierski.daniel.store.SqlStoreQuery;
import pl.madzierski.daniel.user.model.UserQuery;
import pl.madzierski.daniel.wallet.SqlWalletQuery;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = true, exclude = {"wallet", "store"})
@Entity
@Getter
@Setter(AccessLevel.PRIVATE)
@Table(name = "receipt")
class SqlReceipt extends VersionedEntity {

    private String name;
    private String description;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id")
    private SqlWalletQuery wallet;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private SqlStoreQuery store;
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
        sqlReceipt.setWallet(receipt.getWallet() != null ? SqlWalletQuery.fromWalletQuery(receipt.getWallet()) : null);
        sqlReceipt.setStore(receipt.getStore() != null ? SqlStoreQuery.fromStoreQuery(receipt.getStore()) : null);
        sqlReceipt.setOwnerId(receipt.getOwner() != null ? receipt.getOwner().id() : null);
        sqlReceipt.setPreferredRevisionId(receipt.getPreferredRevision() != null ? receipt.getPreferredRevision().id() : null);
        return sqlReceipt;
    }

    public Receipt toReceipt() {
        return Receipt.restore(id, version, createdDate, modifiedDate, name, description,
            wallet != null ? wallet.toWalletQuery() : null,
            store != null ? store.toStoreQuery() : null,
            ownerId != null ? new UserQuery(ownerId) : null,
            preferredRevisionId != null ? new ReceiptRevisionQuery(preferredRevisionId) : null);
    }
}