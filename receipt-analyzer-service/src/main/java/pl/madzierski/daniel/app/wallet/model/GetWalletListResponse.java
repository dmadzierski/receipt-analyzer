package pl.madzierski.daniel.app.wallet.model;

import java.util.List;

public record GetWalletListResponse(List<GetWalletListResponseItem> items) {
    public record GetWalletListResponseItem(String id, String name) {
    }

}
