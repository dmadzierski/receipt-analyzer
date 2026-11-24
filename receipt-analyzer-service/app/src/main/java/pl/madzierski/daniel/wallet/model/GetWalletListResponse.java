package pl.madzierski.daniel.wallet.model;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public record GetWalletListResponse(List<GetWalletListResponseItem> items) {

    public GetWalletListResponse(Collection<WalletDto> wallets) {
        this(wallets.stream().map(GetWalletListResponseItem::new).toList());
    }

    public record GetWalletListResponseItem(UUID id, String name) {
        GetWalletListResponseItem(WalletDto wallet) {
            this(wallet.getId(), wallet.getName());
        }
    }

}
