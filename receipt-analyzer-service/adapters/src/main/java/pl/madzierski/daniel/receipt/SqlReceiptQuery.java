package pl.madzierski.daniel.receipt;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import pl.madzierski.daniel.receipt.model.ReceiptQuery;

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

    public ReceiptQuery toReceipt() {
        ReceiptQuery receiptQuery = new ReceiptQuery();
        receiptQuery.setId(this.id);
        return receiptQuery;
    }
}
