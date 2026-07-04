package pl.madzierski.daniel.app.receipt;

import jakarta.persistence.*;
import lombok.*;
import pl.madzierski.daniel.app.common.model.BaseEntity;
import pl.madzierski.daniel.app.file_group.FileGroupEntity;
import pl.madzierski.daniel.app.receipt.revision.ReceiptRevisionEntity;
import pl.madzierski.daniel.app.wallet.WalletEntity;

import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
@Setter
@Table(name = "receipt")
@Builder
public class ReceiptEntity extends BaseEntity {

    @OneToMany(cascade = {CascadeType.ALL}, mappedBy = "receipt")
    private final Set<ReceiptRevisionEntity> receiptRevisions = new HashSet<>();
    private String name;
    private String description;
    @OneToMany(mappedBy = "receipt")
    private Set<FileGroupEntity> fileGroupEntity;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id")
    private WalletEntity wallet;

    public void addRevision(ReceiptRevisionEntity receiptRevision) {
        this.receiptRevisions.add(receiptRevision);
    }

    public void addFileGroup(FileGroupEntity fileGroup) {
        this.fileGroupEntity.add(fileGroup);
    }
}