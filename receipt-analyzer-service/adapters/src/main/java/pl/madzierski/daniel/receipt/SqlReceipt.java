package pl.madzierski.daniel.receipt;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import pl.madzierski.daniel.store.SqlStoreQuery;
import pl.madzierski.daniel.wallet.SqlWalletQuery;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Getter
@Setter
@Table(name = "receipt")
@Builder
@EntityListeners({AuditingEntityListener.class})
class SqlReceipt {

    @Id
    @UuidGenerator
    private String id;
    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;
    @LastModifiedDate
    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        SqlReceipt that = (SqlReceipt) o;
        return Objects.equals(name, that.name) && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Objects.hashCode(name);
        result = 31 * result + Objects.hashCode(description);
        return result;
    }

    public Receipt toReceipt() {
        Receipt receipt = new Receipt();
        receipt.setId(id);
        receipt.setCreatedDate(createdDate);
        receipt.setModifiedDate(modifiedDate);
        receipt.setName(name);
        receipt.setDescription(description);
        return receipt;
    }
}