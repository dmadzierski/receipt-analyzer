package pl.madzierski.daniel.app.wallet;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.madzierski.daniel.app.wallet.model.CreateWalletRequest;
import pl.madzierski.daniel.app.wallet.model.CreateWalletResponse;
import pl.madzierski.daniel.app.wallet.model.GetWalletDetailsResponse;
import pl.madzierski.daniel.app.wallet.model.GetWalletListResponse;
import pl.madzierski.daniel.exception.AppRuntimeException;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;
import pl.madzierski.daniel.security.SecurityUtils;

import java.util.List;

@Service
@AllArgsConstructor
class WalletService {

    private final WalletRepository walletRepository;

    CreateWalletResponse addWallet(CreateWalletRequest request) {
        WalletEntity savedWallet = walletRepository.save(WalletEntity.builder().name(request.name()).userSub(SecurityUtils.getCurrentUserSub()).build());
        return new CreateWalletResponse(savedWallet.getId(), savedWallet.getName());
    }

    GetWalletListResponse getWallets() {
        List<GetWalletListResponse.GetWalletListResponseItem> wallets = walletRepository.findAllByUserSub(SecurityUtils.getCurrentUserSub()).stream().map(wallet -> new GetWalletListResponse.GetWalletListResponseItem(wallet.getId(), wallet.getName())).toList();
        return new GetWalletListResponse(wallets);
    }

    GetWalletDetailsResponse getWalletDetails(String walletId) {
        WalletEntity walletEntitiesById = walletRepository.findWalletEntitiesByIdAndUserSub(walletId, SecurityUtils.getCurrentUserSub()).orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.WALLET_NOT_FOUND));
        return new GetWalletDetailsResponse(walletEntitiesById.getId(), walletEntitiesById.getName(), walletEntitiesById.getReceipts().stream().map(receipt -> new GetWalletDetailsResponse.GetReceiptItemResponse(receipt.getId(), receipt.getName(), receipt.getDescription(), receipt.getCreatedDate())).toList());
    }
}
