package pl.madzierski.daniel.product_dict;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;


@EqualsAndHashCode(exclude = "productDict")
@Entity
@Table(name = "product_alias")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EntityListeners({AuditingEntityListener.class})
class SqlProductAlias {

    @Id
    @UuidGenerator
    private String id;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private SqlProductDict productDict;



    public static SqlProductAlias fromProductAlias(ProductAlias productAlias, SqlProductDict sqlProductDict) {
        return new SqlProductAlias(
            productAlias.getId(),
            productAlias.getCreatedDate(),
            productAlias.getModifiedDate(),
            productAlias.getName(),
            sqlProductDict
        );
    }

    public ProductAlias toProductAlias(ProductDict productDict) {
        return new ProductAlias(
            this.id,
            this.createdDate,
            this.modifiedDate,
            this.name,
            productDict
        );
    }
}