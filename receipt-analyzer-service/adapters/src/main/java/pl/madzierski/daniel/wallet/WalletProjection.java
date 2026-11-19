package pl.madzierski.daniel.wallet;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.madzierski.daniel.event.IntegrationEvent;
import pl.madzierski.daniel.readmodel.Projection;
import pl.madzierski.daniel.wallet.event.WalletChanged;

@Component
@RequiredArgsConstructor
class WalletProjection implements Projection {

    private final MongoWalletRepository repository;

    @Override
    public void apply(IntegrationEvent event) {
        if (event instanceof WalletChanged changed) repository.save(MongoWallet.fromEvent(changed));
    }

    @Override
    public void clear() {
        repository.deleteAll();
    }
}
