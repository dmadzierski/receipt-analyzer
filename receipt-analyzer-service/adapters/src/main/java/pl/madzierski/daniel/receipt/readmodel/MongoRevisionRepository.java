package pl.madzierski.daniel.receipt.readmodel;

import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import pl.madzierski.daniel.product_dict.ProductDictQueryRepository;
import pl.madzierski.daniel.receipt.ReceiptItemQueryRepository;
import pl.madzierski.daniel.receipt.ReceiptRevisionQueryRepository;
import pl.madzierski.daniel.receipt.model.ReceiptItemDto;
import pl.madzierski.daniel.receipt.model.ReceiptRevisionDto;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

interface MongoRevisionRepository extends MongoRepository<MongoRevision, UUID> {
    List<MongoRevision> findAllByReceiptId(UUID receiptId);

    void deleteAllByReceiptId(UUID receiptId);
}

@Repository
@RequiredArgsConstructor
class ReceiptRevisionQueryRepositoryImpl implements ReceiptRevisionQueryRepository {

    private final MongoReceiptRepository receiptRepository;
    private final MongoRevisionRepository revisionRepository;

    @Override
    public List<ReceiptRevisionDto> getRevisionsByReceiptId(UUID receiptId, UUID ownerId) {
        return receiptRepository.findByIdAndOwnerId(receiptId, ownerId)
            .map(receipt -> revisionRepository.findAllByReceiptId(receiptId).stream().map(revision -> revision.toDto(receipt)).toList())
            .orElse(List.of());
    }

    @Override
    public Optional<ReceiptRevisionDto> getRevisionById(UUID revisionId, UUID ownerId) {
        return revisionRepository.findById(revisionId)
            .flatMap(revision -> receiptRepository.findByIdAndOwnerId(revision.receiptId(), ownerId).map(revision::toDto));
    }
}

@Repository
@RequiredArgsConstructor
class ReceiptItemQueryRepositoryImpl implements ReceiptItemQueryRepository {

    private final MongoRevisionRepository revisionRepository;
    private final ProductDictQueryRepository productDictQueryRepository;

    @Override
    public List<ReceiptItemDto> findReceiptItemsByRevisionId(UUID revisionId) {
        return revisionRepository.findById(revisionId)
            .map(revision -> revision.toItemDtos(productNames(revision)))
            .orElse(List.of());
    }

    private Map<UUID, String> productNames(MongoRevision revision) {
        return productDictQueryRepository.findNamesByIds(revision.productIds());
    }
}
