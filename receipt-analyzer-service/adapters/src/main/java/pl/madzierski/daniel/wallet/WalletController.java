package pl.madzierski.daniel.wallet;

import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.madzierski.daniel.user.model.AuthenticatedUser;
import pl.madzierski.daniel.wallet.model.CreateWalletRequest;
import pl.madzierski.daniel.wallet.model.CreateWalletResponse;
import pl.madzierski.daniel.wallet.model.GetWalletDetailsResponse;
import pl.madzierski.daniel.wallet.model.GetWalletListResponse;

import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping(path = "/wallets", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
class WalletController {

    private final WalletFacade walletFacade;

    @PostMapping
    ResponseEntity<CreateWalletResponse> addWallet(AuthenticatedUser user, @RequestBody CreateWalletRequest request) {
        return ResponseEntity.ok(this.walletFacade.addWallet(user, request));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<GetWalletListResponse> getWallets(AuthenticatedUser user) {
        return ResponseEntity.ok(this.walletFacade.getWallets(user));
    }

    @GetMapping(path = "/{walletId}")
    ResponseEntity<GetWalletDetailsResponse> getWalletDetails(AuthenticatedUser user, @PathVariable UUID walletId) {
        return ResponseEntity.ok(this.walletFacade.getWalletDetails(user, walletId));
    }
}
