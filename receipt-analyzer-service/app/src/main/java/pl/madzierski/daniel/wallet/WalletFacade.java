package pl.madzierski.daniel.wallet;

import lombok.AllArgsConstructor;
import pl.madzierski.daniel.exception.AppRuntimeException;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;
import pl.madzierski.daniel.receipt.ReceiptQueryRepository;
import pl.madzierski.daniel.receipt.projection.WalletReceiptProjection;
import pl.madzierski.daniel.store.StoreQueryRepository;
import pl.madzierski.daniel.store.model.StoreDto;
import pl.madzierski.daniel.user.model.AuthenticatedUser;
import pl.madzierski.daniel.wallet.model.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
public class WalletFacade {

    private final WalletRepository walletRepository;
    private final WalletQueryRepository walletQueryRepository;
    private final ReceiptQueryRepository receiptQueryRepository;
    private final StoreQueryRepository storeQueryRepository;

    CreateWalletResponse addWallet(AuthenticatedUser user, CreateWalletRequest request) {
        Wallet savedWallet = walletRepository.save(Wallet.builder().name(request.name()).owner(user.toUserQuery()).build());
        return new CreateWalletResponse(savedWallet.getId(), savedWallet.getName());
    }

    GetWalletListResponse getWallets(AuthenticatedUser user) {
        List<GetWalletListResponse.GetWalletListResponseItem> wallets =
            walletQueryRepository.findAllByUser(user.id()).stream().map(wallet -> new GetWalletListResponse.GetWalletListResponseItem(wallet.getId(), wallet.getName())).toList();
        return new GetWalletListResponse(wallets);
    }

    GetWalletDetailsResponse getWalletDetails(AuthenticatedUser user, UUID walletId) {
        WalletDto walletDetails = walletQueryRepository.getWalletDetails(walletId, user.id())
            .orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.WALLET_NOT_FOUND));
        Collection<WalletReceiptProjection> walletReceipts = receiptQueryRepository.getWalletReceipts(walletId);
        Set<UUID> storeIds = walletReceipts.stream()
            .map(WalletReceiptProjection::storeId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        Map<UUID, StoreDto> stores = storeIds.isEmpty() ? Map.of() : storeQueryRepository.findStoresByIds(storeIds).stream()
            .collect(Collectors.toMap(StoreDto::getId, Function.identity()));
        return new GetWalletDetailsResponse(walletDetails.getId(), walletDetails.getName(), walletReceipts.stream()
            .map(receipt -> new GetWalletDetailsResponse.Receipt(
                receipt.id(),
                receipt.name(),
                receipt.paymentDate() != null ? receipt.paymentDate().toLocalDate() : null,
                receipt.isCorrect(),
                receipt.totalPrice(),
                receipt.createdDate(),
                receipt.storeId() != null ? stores.get(receipt.storeId()) : null))
            .toList());
    }
}
