package pl.madzierski.daniel.wallet.readmodel;

import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import pl.madzierski.daniel.wallet.WalletQueryRepository;
import pl.madzierski.daniel.wallet.model.WalletDto;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface MongoWalletRepository extends MongoRepository<MongoWallet, UUID> {
    List<MongoWallet> findAllByOwnerId(UUID ownerId);

    Optional<MongoWallet> findByIdAndOwnerId(UUID id, UUID ownerId);
}

@Repository
@RequiredArgsConstructor
class WalletQueryRepositoryImpl implements WalletQueryRepository {

    private final MongoWalletRepository repository;

    @Override
    public Collection<WalletDto> findAllByUser(UUID userId) {
        return repository.findAllByOwnerId(userId).stream().map(MongoWallet::toDto).toList();
    }

    @Override
    public Optional<WalletDto> getWalletDetails(UUID walletId, UUID ownerId) {
        return repository.findByIdAndOwnerId(walletId, ownerId).map(MongoWallet::toDto);
    }
}
