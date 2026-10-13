package pl.madzierski.daniel.product_dict;

import lombok.*;
import pl.madzierski.daniel.user.model.UserQuery;

import java.time.LocalDateTime;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
class ProductCategory {

    private String id;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String name;
    private UserQuery user;

    public ProductCategory(String name, UserQuery user) {
        this.name = name;
        this.user = user;
    }

}