package pl.madzierski.daniel.receipt;

import lombok.*;
import pl.madzierski.daniel.file_group.model.FileGroupQuery;
import pl.madzierski.daniel.wallet.model.WalletQuery;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
class Receipt {

    private final Set<FileGroupQuery> fileGroups = new HashSet<>();
    private final Set<ReceiptRevision> receiptRevisions = new HashSet<>();
    private String id;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String name;
    private String description;
    private WalletQuery wallet;

    public void addRevision(ReceiptRevision receiptRevision) {
        this.receiptRevisions.add(receiptRevision);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Receipt that = (Receipt) o;
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