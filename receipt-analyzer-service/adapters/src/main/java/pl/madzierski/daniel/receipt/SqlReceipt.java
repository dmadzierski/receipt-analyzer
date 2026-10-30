package pl.madzierski.daniel.receipt;

import jakarta.persistence.*;
import lombok.*;
import pl.madzierski.daniel.common.entity.BaseEntity;
import pl.madzierski.daniel.store.SqlStoreQuery;
import pl.madzierski.daniel.wallet.SqlWalletQuery;

import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, exclude = {"receiptRevisions", "wallet", "store"})
@Entity
@Getter
@Setter
@Table(name = "receipt")
@Builder
class SqlReceipt extends BaseEntity {

    private String name;
    private String description;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id")
    private SqlWalletQuery wallet;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "receipt")
    private final Set<SqlReceiptRevision> receiptRevisions = new HashSet<>();
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private SqlStoreQuery store;

    public static SqlReceipt fromReceipt(Receipt receipt) {
        SqlReceipt sqlReceipt = new SqlReceipt();
        sqlReceipt.setId(receipt.getId());
        sqlReceipt.setCreatedDate(receipt.getCreatedDate());
        sqlReceipt.setModifiedDate(receipt.getModifiedDate());
        sqlReceipt.setName(receipt.getName());
        sqlReceipt.setDescription(receipt.getDescription());
        sqlReceipt.setWallet(receipt.getWallet() != null ? SqlWalletQuery.fromWalletQuery(receipt.getWallet()) : null);
        sqlReceipt.setStore(receipt.getStore() != null ? SqlStoreQuery.fromStoreQuery(receipt.getStore()) : null);
        return sqlReceipt;
    }



    public Receipt toReceipt() {
        Receipt receipt = new Receipt();
        receipt.setId(id);
        receipt.setCreatedDate(createdDate);
        receipt.setModifiedDate(modifiedDate);
        receipt.setName(name);
        receipt.setDescription(description);
        receipt.setWallet(wallet != null ? wallet.toWalletQuery() : null);
        receipt.setStore(store != null ? store.toStoreQuery() : null);
        return receipt;
    }
}