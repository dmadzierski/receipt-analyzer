package pl.madzierski.daniel.receipt;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import pl.madzierski.daniel.common.entity.BaseEntity;
import pl.madzierski.daniel.receipt.model.ReceiptQuery;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "receipt")
@Getter
@Setter
@NoArgsConstructor
public class SqlReceiptQuery extends BaseEntity {

    @Column(name = "owner_id", insertable = false, updatable = false)
    private UUID ownerId;

    public SqlReceiptQuery(UUID id) {
        this.id = id;
    }

    public ReceiptQuery toReceipt() {
        return new ReceiptQuery(id);
    }
}
