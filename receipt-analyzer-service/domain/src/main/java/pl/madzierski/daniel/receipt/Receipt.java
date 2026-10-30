package pl.madzierski.daniel.receipt;

import lombok.*;
import pl.madzierski.daniel.file_group.model.FileGroupQuery;
import pl.madzierski.daniel.store.model.StoreQuery;
import pl.madzierski.daniel.wallet.model.WalletQuery;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode(exclude = {"wallet", "receiptRevisions", "fileGroups"})
class Receipt {

    private final Set<FileGroupQuery> fileGroups = new HashSet<>();
    private final Set<ReceiptRevision> receiptRevisions = new HashSet<>();
    private UUID id;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String name;
    private String description;
    private WalletQuery wallet;
    private StoreQuery store;

}