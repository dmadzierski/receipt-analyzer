package pl.madzierski.daniel.receipt;

import jakarta.persistence.*;
import lombok.*;
import pl.madzierski.daniel.common.entity.BaseEntity;
import pl.madzierski.daniel.file_group.SqlFileGroupQuery;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true, exclude = {"items", "childReceiptRevisions", "receipt", "parentReceiptRevision", "sourceFileGroup"})
@Entity
@Table(name = "receipt_revision")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class SqlReceiptRevision extends BaseEntity {

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
    @Builder.Default
    private Set<SqlReceiptItem> items = new HashSet<>();
    @Getter(AccessLevel.NONE)
    @OneToMany(mappedBy = "parentReceiptRevision")
    @Builder.Default
    private Set<SqlReceiptRevision> childReceiptRevisions = new HashSet<>();
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_file_group_id")
    private SqlFileGroupQuery sourceFileGroup;

    public static SqlReceiptRevision fromReceiptRevision(ReceiptRevision revision) {
        SqlReceiptRevision sqlReceiptRevision = new SqlReceiptRevision();
        sqlReceiptRevision.setId(revision.getId());
        sqlReceiptRevision.setRevision(revision.getRevision());
        sqlReceiptRevision.setName(revision.getName());
        sqlReceiptRevision.setTotalPrice(revision.getTotalPrice());
        sqlReceiptRevision.setPaymentDate(revision.getPaymentDate());
        sqlReceiptRevision.setIsPreferredRevision(revision.getIsPreferredRevision());
        sqlReceiptRevision.setResolver(revision.getResolver());
        sqlReceiptRevision.setIsCorrect(revision.getIsCorrect());
        sqlReceiptRevision.setReceipt(SqlReceipt.fromReceipt(revision.getReceipt()));
        sqlReceiptRevision.setItems(revision.getItems() != null ? new HashSet<>(revision.getItems()
            .stream()
            .map(SqlReceiptItem::fromReceiptItem)
            .toList()) : Collections.emptySet());
        if (revision.getParentReceiptRevision() != null) {
            SqlReceiptRevision parent = new SqlReceiptRevision();
            parent.setId(revision.getParentReceiptRevision().getId());
            sqlReceiptRevision.setParentReceiptRevision(parent);
        }
        sqlReceiptRevision.items.forEach(item -> item.setReceiptRevision(sqlReceiptRevision));
        if (revision.getSourceFileGroup() != null) {
            sqlReceiptRevision.setSourceFileGroup(SqlFileGroupQuery.fromFileGroupQuery(revision.getSourceFileGroup()));
        }
        return sqlReceiptRevision;
    }
    
    public ReceiptRevision toReceiptRevision() {
        return ReceiptRevision.builder()
            .id(id)
            .name(name)
            .revision(revision)
            .createdDate(createdDate)
            .modifiedDate(modifiedDate)
            .totalPrice(totalPrice)
            .paymentDate(paymentDate)
            .isPreferredRevision(isPreferredRevision)
            .isCorrect(isCorrect)
            .receipt(receipt != null ? receipt.toReceipt() : null)
            .resolver(resolver)
            .parentReceiptRevision(parentReceiptRevision != null ? ReceiptRevision.builder().id(parentReceiptRevision.getId()).build() : null)
            .sourceFileGroup(sourceFileGroup != null ? sourceFileGroup.toFileGroupQuery() : null)
            .childReceiptRevisions(childReceiptRevisions.stream()
                .map(SqlReceiptRevision::toReceiptRevision)
                .collect(Collectors.toSet()))
            .items(items.stream()
                .map(SqlReceiptItem::toReceiptItem)
                .collect(Collectors.toSet()))
            .build();
    }
}