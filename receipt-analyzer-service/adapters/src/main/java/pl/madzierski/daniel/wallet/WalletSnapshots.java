package pl.madzierski.daniel.wallet;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.madzierski.daniel.event.IntegrationEvent;
import pl.madzierski.daniel.readmodel.ReadModelSnapshotSource;
import pl.madzierski.daniel.wallet.event.WalletChanged;

import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
class WalletSnapshots implements ReadModelSnapshotSource {

    private final SqlWalletRepository repository;

    static WalletChanged changed(Wallet wallet) {
        return new WalletChanged(wallet.getId(), wallet.getVersion(), wallet.getOwner().id(), wallet.getName(), wallet.getCreatedDate());
    }

    @Override
    public int order() {
        return 10;
    }

    @Override
    public Stream<IntegrationEvent> snapshots() {
        return repository.findAll().stream().map(SqlWallet::toWallet).map(WalletSnapshots::changed);
    }
}
