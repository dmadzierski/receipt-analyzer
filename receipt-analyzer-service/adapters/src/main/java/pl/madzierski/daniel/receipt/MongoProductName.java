package pl.madzierski.daniel.receipt;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

@Document("product_dicts")
record MongoProductName(@Id UUID id, String name) {
}

interface MongoProductNameRepository extends MongoRepository<MongoProductName, UUID> {
}
