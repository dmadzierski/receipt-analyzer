package pl.madzierski.daniel.receipt;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import pl.madzierski.daniel.receipt.scan_resolver.ReceiptResolverStrategyType;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "receipt_revision")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners({AuditingEntityListener.class})
@Builder
class ReceiptRevisionEntity {
    @Getter(AccessLevel.NONE)
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "receiptRevision")
    private final Set<ReceiptItemEntity> items = new HashSet<>();
    @Getter(AccessLevel.NONE)
    @OneToMany(mappedBy = "parentReceiptRevision")
    private final Set<ReceiptRevisionEntity> childReceiptRevisions = new HashSet<>();
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
    private String revision;
    private ReceiptResolverStrategyType resolver;
    private String brand;
    private Double totalPrice;
    private LocalDateTime payingDate;
    private String address;
    private Boolean isPreferredRevision;
    private Boolean isCorrect;
    @ManyToOne(fetch = FetchType.LAZY)
    private ReceiptEntity receipt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_receipt_revision_id")
    private ReceiptRevisionEntity parentReceiptRevision;

    public Set<ReceiptItemEntity> getItems() {
        return Collections.unmodifiableSet(items);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ReceiptRevisionEntity that = (ReceiptRevisionEntity) o;
        return Objects.equals(name, that.name) && Objects.equals(revision, that.revision) && resolver == that.resolver && Objects.equals(brand, that.brand) && Objects.equals(totalPrice, that.totalPrice) && Objects.equals(payingDate, that.payingDate) && Objects.equals(address, that.address) && Objects.equals(isPreferredRevision, that.isPreferredRevision) && Objects.equals(isCorrect, that.isCorrect);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, revision, resolver, brand, totalPrice, payingDate, address, isPreferredRevision, isCorrect);
    }

    public void addItems(Set<ReceiptItemEntity> items) {
        this.items.addAll(items);
    }
}