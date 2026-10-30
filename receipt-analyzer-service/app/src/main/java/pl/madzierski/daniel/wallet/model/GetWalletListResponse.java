package pl.madzierski.daniel.wallet.model;

import java.util.List;
import java.util.UUID;

public record GetWalletListResponse(List<GetWalletListResponseItem> items) {
    public record GetWalletListResponseItem(UUID id, String name) {
    }

}
