package pl.madzierski.daniel.wallet;

import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.madzierski.daniel.wallet.model.CreateWalletRequest;
import pl.madzierski.daniel.wallet.model.CreateWalletResponse;
import pl.madzierski.daniel.wallet.model.GetWalletDetailsResponse;
import pl.madzierski.daniel.wallet.model.GetWalletListResponse;

@AllArgsConstructor
@RestController
@RequestMapping(path = "/wallets", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
class WalletController {

    private final WalletFacade walletFacade;

    @PostMapping
    ResponseEntity<CreateWalletResponse> addWallet(@AuthenticationPrincipal Jwt jwt, @RequestBody CreateWalletRequest request) {
        return ResponseEntity.ok(this.walletFacade.addWallet(request, jwt.getSubject()));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<GetWalletListResponse> getWallets(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(this.walletFacade.getWallets(jwt.getSubject()));
    }

    @GetMapping(path = "/{walletId}")
    ResponseEntity<GetWalletDetailsResponse> getWalletDetails(@PathVariable String walletId) {
        return ResponseEntity.ok(this.walletFacade.getWalletDetails(walletId));
    }
}