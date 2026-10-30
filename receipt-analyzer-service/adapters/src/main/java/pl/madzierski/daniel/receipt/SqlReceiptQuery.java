package pl.madzierski.daniel.receipt;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import pl.madzierski.daniel.common.entity.BaseEntity;
import pl.madzierski.daniel.receipt.model.ReceiptQuery;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "receipt")
@Getter
@Setter
@NoArgsConstructor
public class SqlReceiptQuery extends BaseEntity {

    public SqlReceiptQuery(String id) {
        this.id = id;
    }

    public ReceiptQuery toReceipt() {
        ReceiptQuery receiptQuery = new ReceiptQuery();
        receiptQuery.setId(this.id);
        return receiptQuery;
    }
}
