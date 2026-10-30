package pl.madzierski.daniel.product_dict.model;

import lombok.*;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@EqualsAndHashCode
public class ProductAliasDto {
    private UUID id;
    private String name;
}
