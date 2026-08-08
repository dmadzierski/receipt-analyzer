package pl.madzierski.daniel.receipt.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.madzierski.daniel.wallet.model.WalletQuery;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptQuery {
    private String id;
    private WalletQuery wallet;
}
