package pl.madzierski.daniel.receipt;

import jakarta.persistence.*;
import lombok.*;
import pl.madzierski.daniel.common.entity.VersionedEntity;
import pl.madzierski.daniel.digitization.ReceiptResolverStrategyType;
import pl.madzierski.daniel.file_group.SqlFileGroupQuery;
import pl.madzierski.daniel.receipt.model.ReceiptQuery;
import pl.madzierski.daniel.receipt.model.ReceiptRevisionQuery;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true, exclude = {"items", "receipt", "parentReceiptRevision", "sourceFileGroup"})
@Entity
@Table(name = "receipt_revision")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class SqlReceiptRevision extends VersionedEntity {

    private String revision;
    @Enumerated(EnumType.STRING)
    private ReceiptResolverStrategyType resolver;
    private BigDecimal totalPrice;
    private LocalDateTime paymentDate;
    private Boolean isCorrect;
    @ManyToOne(fetch = FetchType.LAZY)
    private SqlReceiptQuery receipt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_receipt_revision_id")
    private SqlReceiptRevisionQuery parentReceiptRevision;
    @Getter(AccessLevel.NONE)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "receipt_revision_id", nullable = false)
    @Builder.Default
    private Set<SqlReceiptItem> items = new HashSet<>();
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_file_group_id")
    private SqlFileGroupQuery sourceFileGroup;

    public static SqlReceiptRevision fromReceiptRevision(ReceiptRevision revision) {
        SqlReceiptRevision sqlReceiptRevision = new SqlReceiptRevision();
        sqlReceiptRevision.setId(revision.getId());
        sqlReceiptRevision.setVersion(revision.getVersion());
        sqlReceiptRevision.setCreatedDate(revision.getCreatedDate());
        sqlReceiptRevision.setModifiedDate(revision.getModifiedDate());
        sqlReceiptRevision.setRevision(revision.getRevision());
        sqlReceiptRevision.setTotalPrice(revision.getTotalPrice());
        sqlReceiptRevision.setPaymentDate(revision.getPaymentDate());
        sqlReceiptRevision.setResolver(revision.getResolver());
        sqlReceiptRevision.setIsCorrect(revision.getIsCorrect());
        if (revision.getReceipt() != null) {
            sqlReceiptRevision.setReceipt(new SqlReceiptQuery(revision.getReceipt().id()));
        }
        sqlReceiptRevision.setItems(revision.getItems() != null ? new HashSet<>(revision.getItems()
            .stream()
            .map(SqlReceiptItem::fromReceiptItem)
            .toList()) : Collections.emptySet());
        if (revision.getParentReceiptRevision() != null) {
            sqlReceiptRevision.setParentReceiptRevision(new SqlReceiptRevisionQuery(revision.getParentReceiptRevision().id()));
        }
        if (revision.getSourceFileGroup() != null) {
            sqlReceiptRevision.setSourceFileGroup(SqlFileGroupQuery.fromFileGroupQuery(revision.getSourceFileGroup()));
        }
        return sqlReceiptRevision;
    }
    
    public ReceiptRevision toReceiptRevision() {
        return ReceiptRevision.restore(id, version, createdDate, modifiedDate, revision, resolver, totalPrice, paymentDate,
            isCorrect, receipt != null ? new ReceiptQuery(receipt.getId()) : null,
            parentReceiptRevision != null ? new ReceiptRevisionQuery(parentReceiptRevision.getId()) : null,
            items.stream()
                .map(SqlReceiptItem::toReceiptItem)
                .collect(Collectors.toSet()),
            sourceFileGroup != null ? sourceFileGroup.toFileGroupQuery() : null);
    }
}
