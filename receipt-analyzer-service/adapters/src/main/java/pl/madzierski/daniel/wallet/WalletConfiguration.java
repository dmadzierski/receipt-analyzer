package pl.madzierski.daniel.wallet;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class WalletConfiguration {

    @Bean
    WalletFacade walletFacade(WalletRepository walletRepository, WalletQueryRepository walletQueryRepository) {
        return new WalletFacade(walletRepository, walletQueryRepository);
    }
}
