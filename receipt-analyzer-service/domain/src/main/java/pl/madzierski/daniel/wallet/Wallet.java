package pl.madzierski.daniel.wallet;

import lombok.*;
import pl.madzierski.daniel.receipt.model.ReceiptQueryEntity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
class Wallet {

    private final Set<ReceiptQueryEntity> receipts = new HashSet<>();
    private String id;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String name;
    private String userSub;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Wallet wallet = (Wallet) o;
        return Objects.equals(name, wallet.name) && Objects.equals(userSub, wallet.userSub);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Objects.hashCode(name);
        result = 31 * result + Objects.hashCode(userSub);
        return result;
    }
}
