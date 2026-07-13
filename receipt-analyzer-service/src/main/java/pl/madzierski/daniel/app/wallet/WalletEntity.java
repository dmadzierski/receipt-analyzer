package pl.madzierski.daniel.app.wallet;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;
import org.apache.commons.lang3.exception.ExceptionUtils;
import pl.madzierski.daniel.app.common.model.BaseEntity;
import pl.madzierski.daniel.app.receipt.ReceiptEntity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
@Setter
@Table(name = "wallet")
@Builder
public class WalletEntity extends BaseEntity {
    private String name;
    @Getter(AccessLevel.NONE)
    @OneToMany(mappedBy = "wallet")
    private final Set<ReceiptEntity> receipts = new HashSet<>();
    @Column(name = "user_sub")
    private String userSub;

    public Set<ReceiptEntity> getReceipts() {
        return receipts.stream().collect(Collectors.toUnmodifiableSet());
    }
}
