package pl.madzierski.daniel.wallet.model;

import java.util.UUID;

public record GetWalletDetailsResponse(
    UUID id,
    String name
) {
}
