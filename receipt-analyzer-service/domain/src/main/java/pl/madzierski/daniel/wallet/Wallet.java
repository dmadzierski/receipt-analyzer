package pl.madzierski.daniel.wallet;

import lombok.*;
import pl.madzierski.daniel.receipt.model.ReceiptQuery;
import pl.madzierski.daniel.user.model.UserQuery;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode(exclude = "receipts")
class Wallet {

    private final Set<ReceiptQuery> receipts = new HashSet<>();
    private String id;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String name;
    private UserQuery user;
}
