package pl.madzierski.daniel.readmodel;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

@Document("projected_versions")
record ProjectedVersion(@Id UUID aggregateId, long version, boolean deleted) {

    boolean isOlderThan(long otherVersion) {
        return !deleted && version < otherVersion;
    }
}

interface ProjectedVersionRepository extends MongoRepository<ProjectedVersion, UUID> {
}
