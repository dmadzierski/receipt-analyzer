package pl.madzierski.daniel.app.product_dict.model;

import java.util.Collection;
import java.util.List;

public record UpdateProductDictListResponse(Collection<UpdateProductDict> items) {
    public record UpdateProductDict(String id, String name, String productCategoryId, List<Alias> aliases) {
        public record Alias(String id, String alias) {
        }
    }

}