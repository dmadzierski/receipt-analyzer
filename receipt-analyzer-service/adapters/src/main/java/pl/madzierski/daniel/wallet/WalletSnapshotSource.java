package pl.madzierski.daniel.wallet;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.madzierski.daniel.event.IntegrationEvent;
import pl.madzierski.daniel.readmodel.ReadModelSnapshotSource;

import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
class WalletSnapshotSource implements ReadModelSnapshotSource {

    private final SqlWalletRepository repository;

    @Override
    public int order() {
        return 10;
    }

    @Override
    public Stream<IntegrationEvent> snapshots() {
        return repository.findAll().stream().map(SqlWallet::toWallet).map(WalletEventMapper::changed);
    }
}
