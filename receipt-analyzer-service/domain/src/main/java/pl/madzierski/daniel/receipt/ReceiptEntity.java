package pl.madzierski.daniel.receipt;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import pl.madzierski.daniel.file_group.model.FileGroupQueryEntity;
import pl.madzierski.daniel.wallet.model.WalletQueryEntity;

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
class ReceiptEntity {

    @Getter(AccessLevel.NONE)
    @OneToMany
    private final Set<FileGroupQueryEntity> fileGroups = new HashSet<>();
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "receipt")
    private final Set<ReceiptRevisionEntity> receiptRevisions = new HashSet<>();
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
    private WalletQueryEntity wallet;

    public void addRevision(ReceiptRevisionEntity receiptRevision) {
        this.receiptRevisions.add(receiptRevision);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ReceiptEntity that = (ReceiptEntity) o;
        return Objects.equals(name, that.name) && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Objects.hashCode(name);
        result = 31 * result + Objects.hashCode(description);
        return result;
    }

}