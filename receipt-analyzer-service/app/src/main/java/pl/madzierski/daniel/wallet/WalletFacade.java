package pl.madzierski.daniel.wallet;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.madzierski.daniel.exception.AppRuntimeException;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;
import pl.madzierski.daniel.receipt.ReceiptQueryRepository;
import pl.madzierski.daniel.receipt.model.ReceiptDto;
import pl.madzierski.daniel.wallet.model.*;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletFacade {

    private final WalletRepository walletRepository;
    private final WalletQueryRepository walletQueryRepository;
    private final ReceiptQueryRepository receiptQueryRepository;

    CreateWalletResponse addWallet(CreateWalletRequest request, String userSub) {
        WalletEntity savedWallet = walletRepository.save(WalletEntity.builder().name(request.name()).userSub(userSub).build());
        return new CreateWalletResponse(savedWallet.getId(), savedWallet.getName());
    }

    GetWalletListResponse getWallets(String userSub) {
        List<GetWalletListResponse.GetWalletListResponseItem> wallets =
            walletQueryRepository.findAllByUserSub(userSub).stream().map(wallet -> new GetWalletListResponse.GetWalletListResponseItem(wallet.getId(), wallet.getName())).toList();
        return new GetWalletListResponse(wallets);
    }

    GetWalletDetailsResponse getWalletDetails(String walletId) {
        WalletDto walletDetails = walletQueryRepository.getWalletDetails(walletId).orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.WALLET_NOT_FOUND));
        Collection<ReceiptDto> walletReceipts = receiptQueryRepository.getWalletReceipts(walletId);
        return new GetWalletDetailsResponse(walletDetails.getId(), walletDetails.getName(), walletReceipts.stream().map(receipt -> new GetWalletDetailsResponse.GetReceiptItemResponse(receipt.getId(), receipt.getName(), receipt.getDescription(), receipt.getCreatedDate())).toList());
    }
}
