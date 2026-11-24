package pl.madzierski.daniel.receipt;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import pl.madzierski.daniel.common.entity.BaseEntity;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "receipt_revision")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class SqlReceiptRevisionQuery extends BaseEntity {

    SqlReceiptRevisionQuery(UUID id) {
        this.id = id;
    }
}
