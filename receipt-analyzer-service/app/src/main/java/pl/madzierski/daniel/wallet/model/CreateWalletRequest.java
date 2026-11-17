package pl.madzierski.daniel.wallet.model;

import jakarta.validation.constraints.NotBlank;

public record CreateWalletRequest(
    @NotBlank
    String name) {
}
