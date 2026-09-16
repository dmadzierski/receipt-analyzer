package pl.madzierski.daniel.wallet;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.madzierski.daniel.receipt.ReceiptQueryRepository;
import pl.madzierski.daniel.user.UserFacade;

@Configuration
class WalletConfiguration {

    @Bean
    WalletFacade walletFacade(WalletRepository walletRepository, WalletQueryRepository walletQueryRepository,
                              ReceiptQueryRepository receiptQueryRepository, UserFacade userFacade) {
        return new WalletFacade(walletRepository, walletQueryRepository, receiptQueryRepository, userFacade);
    }
}
