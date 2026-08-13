package pl.madzierski.daniel.product_dict.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collection;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class ProductDto {

    private String id;
    private String name;
    private Collection<ProductAliasDto> aliases;

    public ProductDto(String id) {
        this.id = id;
    }

    public void addAlias(ProductAliasDto productAliasDto) {
        aliases.add(productAliasDto);
    }
}
