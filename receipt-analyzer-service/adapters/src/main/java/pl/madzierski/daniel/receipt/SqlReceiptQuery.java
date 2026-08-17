package pl.madzierski.daniel.receipt;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import pl.madzierski.daniel.receipt.model.ReceiptQuery;
import pl.madzierski.daniel.wallet.SqlWalletQuery;

@Entity
@Table(name = "receipt")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SqlReceiptQuery {
    @Id
    @UuidGenerator
    private String id;

    public static SqlReceiptQuery fromReceipt(ReceiptQuery receipt) {
        SqlReceiptQuery sqlReceiptQuery = new SqlReceiptQuery();
        sqlReceiptQuery.id = receipt.getId();
        return sqlReceiptQuery;
    }

    public ReceiptQuery toReceipt() {
        ReceiptQuery receiptQuery = new ReceiptQuery();
        receiptQuery.setId(this.id);
        return receiptQuery;
    }
}
