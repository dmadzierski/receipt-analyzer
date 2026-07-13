package pl.madzierski.daniel.receipt;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import pl.madzierski.daniel.product_dict.model.ProductDictQueryEntity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "receipt_item")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners({AuditingEntityListener.class})
class ReceiptItemEntity {

    @Getter(AccessLevel.NONE)
    @OneToMany(mappedBy = "parentItem")
    private final Set<ReceiptItemEntity> childItems = new HashSet<>();
    @Id
    @UuidGenerator
    private String id;
    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;
    @LastModifiedDate
    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;
    @ManyToOne(fetch = FetchType.LAZY)
    private ReceiptRevisionEntity receiptRevision;
    private String name;
    @ManyToOne
    @JoinColumn(name = "product_dict_id")
    private ProductDictQueryEntity nameDict;
    private Double amount;
    private Double unitPrice;
    private Double discount;
    private Double totalPrice;
    private Integer position;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_item_id")
    private ReceiptItemEntity parentItem;

    public ReceiptItemEntity(String id) {
        this.id = id;
    }

    public ReceiptItemEntity(ReceiptRevisionEntity receiptRevision, String name, ProductDictQueryEntity nameDict, Double amount,
                             Double unitPrice, Double discount, Double totalPrice, Integer position, ReceiptItemEntity parentItem) {
        this.receiptRevision = receiptRevision;
        this.name = name;
        this.nameDict = nameDict;
        this.amount = amount;
        this.unitPrice = unitPrice;
        this.discount = discount;
        this.totalPrice = totalPrice;
        this.position = position;
        this.parentItem = parentItem;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ReceiptItemEntity that = (ReceiptItemEntity) o;
        return Objects.equals(name, that.name) && Objects.equals(amount, that.amount) && Objects.equals(unitPrice, that.unitPrice) && Objects.equals(discount, that.discount) && Objects.equals(totalPrice, that.totalPrice) && Objects.equals(position, that.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, amount, unitPrice, discount, totalPrice, position);
    }

}