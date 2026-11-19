package pl.madzierski.daniel.common.entity;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class VersionedEntity extends BaseEntity {

    @Version
    protected Long version;
}
