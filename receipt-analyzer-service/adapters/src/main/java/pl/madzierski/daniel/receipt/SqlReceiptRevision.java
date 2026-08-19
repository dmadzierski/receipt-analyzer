package pl.madzierski.daniel.receipt;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
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
class SqlReceiptRevision {

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
    @Enumerated(EnumType.STRING)
    private ReceiptResolverStrategyType resolver;
    private BigDecimal totalPrice;
    private LocalDateTime paymentDate;
    private Boolean isPreferredRevision;
    private Boolean isCorrect;
    @ManyToOne(fetch = FetchType.LAZY)
    private SqlReceipt receipt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_receipt_revision_id")
    private SqlReceiptRevision parentReceiptRevision;
    @Getter(AccessLevel.NONE)
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "receiptRevision")
    private Set<SqlReceiptItem> items = new HashSet<>();
    @Getter(AccessLevel.NONE)
    @OneToMany(mappedBy = "parentReceiptRevision")
    private Set<SqlReceiptRevision> childReceiptRevisions = new HashSet<>();

    public static SqlReceiptRevision fromReceiptRevision(ReceiptRevision revision) {
        SqlReceiptRevision sqlReceiptRevision = new SqlReceiptRevision();
        sqlReceiptRevision.setId(revision.getId());
        sqlReceiptRevision.setName(revision.getName());
        sqlReceiptRevision.setTotalPrice(revision.getTotalPrice());
        sqlReceiptRevision.setPaymentDate(revision.getPaymentDate());
        sqlReceiptRevision.setIsPreferredRevision(revision.getIsPreferredRevision());
        sqlReceiptRevision.setResolver(revision.getResolver());
        sqlReceiptRevision.setIsCorrect(revision.getIsCorrect());
        sqlReceiptRevision.setReceipt(revision.getReceipt() != null ? SqlReceipt.fromReceipt(revision.getReceipt()) : null);
        sqlReceiptRevision.setItems(revision.getItems() != null ? new HashSet<>(revision.getItems().stream().map(SqlReceiptItem::fromReceiptItem).toList()) : Collections.emptySet());
        sqlReceiptRevision.items.forEach(item -> item.setReceiptRevision(sqlReceiptRevision));
        return sqlReceiptRevision;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SqlReceiptRevision that = (SqlReceiptRevision) o;
        return Objects.equals(name, that.name) && Objects.equals(revision, that.revision) && resolver == that.resolver && Objects.equals(totalPrice, that.totalPrice) && Objects.equals(paymentDate, that.paymentDate) && Objects.equals(isPreferredRevision, that.isPreferredRevision) && Objects.equals(isCorrect, that.isCorrect);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, revision, resolver, totalPrice, paymentDate, isPreferredRevision, isCorrect);
    }

    public ReceiptRevision toReceiptRevision() {
        return ReceiptRevision.builder()
            .id(id)
            .name(name)
            .createdDate(createdDate)
            .modifiedDate(modifiedDate)
            .totalPrice(totalPrice)
            .paymentDate(paymentDate)
            .isPreferredRevision(isPreferredRevision)
            .isCorrect(isCorrect)
            .receipt(receipt != null ? receipt.toReceipt() : null)
            .resolver(resolver)
            .items(Collections.unmodifiableSet(items.stream().map(SqlReceiptItem::toReceiptItem).collect(java.util.stream.Collectors.toSet())))
            .build();
    }
}