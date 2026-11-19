package pl.madzierski.daniel.readmodel;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;

@Document("projection_meta")
record ProjectionMeta(@Id String id, Date rebuiltAt) {
    static final String READ_MODEL = "read-model";
}

interface ProjectionMetaRepository extends MongoRepository<ProjectionMeta, String> {
}
