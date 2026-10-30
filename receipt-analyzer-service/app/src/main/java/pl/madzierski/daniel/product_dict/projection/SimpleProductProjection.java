package pl.madzierski.daniel.product_dict.projection;

import java.util.UUID;

public interface SimpleProductProjection {
    UUID getId();

    String getName();

    UUID getAliasId();

    String getAliasName();

    UUID getCategoryId();

    String getCategoryName();
}
