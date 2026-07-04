package pl.madzierski.daniel.app.wallet;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class WalletProvider {

    private final WalletRepository walletRepository;

    public Optional<WalletEntity> findWallet(String walletId) {
        return walletRepository.findById(walletId);
    }
}
