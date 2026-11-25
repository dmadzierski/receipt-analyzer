package pl.madzierski.daniel.receipt.readmodel;

import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import pl.madzierski.daniel.receipt.ReceiptQueryRepository;
import pl.madzierski.daniel.receipt.model.ReceiptDto;
import pl.madzierski.daniel.receipt.model.WalletReceiptDto;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface MongoReceiptRepository extends MongoRepository<MongoReceipt, UUID> {
    @Aggregation(pipeline = {
        "{ $match: { walletId: ?0 } }",
        "{ $lookup: { from: 'revisions', localField: 'preferredRevisionId', foreignField: '_id', as: 'preferred', pipeline: [ { $project: { paymentDate: 1, isCorrect: 1, totalPrice: 1 } } ] } }",
        "{ $unwind: { path: '$preferred', preserveNullAndEmptyArrays: true } }",
        "{ $project: { name: 1, createdDate: 1, storeId: 1, paymentDate: '$preferred.paymentDate', isCorrect: '$preferred.isCorrect', totalPrice: '$preferred.totalPrice' } }"
    })
    List<WalletReceiptDto> findWalletReceipts(UUID walletId);

    Optional<MongoReceipt> findByIdAndOwnerId(UUID id, UUID ownerId);
}

@Repository
@RequiredArgsConstructor
class ReceiptQueryRepositoryImpl implements ReceiptQueryRepository {

    private final MongoReceiptRepository receiptRepository;

    @Override
    public Collection<WalletReceiptDto> getWalletReceipts(UUID walletId) {
        return receiptRepository.findWalletReceipts(walletId);
    }

    @Override
    public Optional<ReceiptDto> findById(UUID receiptId, UUID ownerId) {
        return receiptRepository.findByIdAndOwnerId(receiptId, ownerId).map(MongoReceipt::toDto);
    }
}
