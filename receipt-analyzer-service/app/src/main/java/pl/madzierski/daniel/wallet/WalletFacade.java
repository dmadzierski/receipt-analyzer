package pl.madzierski.daniel.wallet;

import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import pl.madzierski.daniel.exception.AppRuntimeException;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;
import pl.madzierski.daniel.user.model.AuthenticatedUser;
import pl.madzierski.daniel.wallet.model.*;

import java.util.UUID;

@AllArgsConstructor
public class WalletFacade {

    private final WalletRepository walletRepository;
    private final WalletQueryRepository walletQueryRepository;

    @Transactional
    CreateWalletResponse addWallet(AuthenticatedUser user, CreateWalletRequest request) {
        return new CreateWalletResponse(walletRepository.save(Wallet.create(request.name(), user.toUserQuery())).getId());
    }

    GetWalletListResponse getWallets(AuthenticatedUser user) {
        return new GetWalletListResponse(walletQueryRepository.findAllByUser(user.id()));
    }

    public boolean isWalletAccessible(AuthenticatedUser user, UUID walletId) {
        return walletRepository.isAccessible(walletId, user.toUserQuery());
    }

    GetWalletDetailsResponse getWalletDetails(AuthenticatedUser user, UUID walletId) {
        WalletDto walletDetails = walletQueryRepository.getWalletDetails(walletId, user.id())
            .orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.WALLET_NOT_FOUND));
        return new GetWalletDetailsResponse(walletDetails.getId(), walletDetails.getName());
    }
}
