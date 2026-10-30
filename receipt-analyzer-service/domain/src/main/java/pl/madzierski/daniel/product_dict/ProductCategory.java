package pl.madzierski.daniel.product_dict;

import lombok.*;
import pl.madzierski.daniel.user.model.UserQuery;

import java.time.LocalDateTime;
import java.util.UUID;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
class ProductCategory {

    private UUID id;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String name;
    private UserQuery user;

    public ProductCategory(String name, UserQuery user) {
        this.name = name;
        this.user = user;
    }

}