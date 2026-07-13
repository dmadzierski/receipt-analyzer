package pl.madzierski.daniel.app.wallet;

import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.madzierski.daniel.app.wallet.model.CreateWalletRequest;
import pl.madzierski.daniel.app.wallet.model.CreateWalletResponse;
import pl.madzierski.daniel.app.wallet.model.GetWalletDetailsResponse;
import pl.madzierski.daniel.app.wallet.model.GetWalletListResponse;

@AllArgsConstructor
@RestController
@RequestMapping(path = "/wallets", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
class WalletController {

    private final WalletService walletService;

    @PostMapping
    ResponseEntity<CreateWalletResponse> addWallet(@RequestBody CreateWalletRequest request) {
        return ResponseEntity.ok(this.walletService.addWallet(request));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<GetWalletListResponse> getWallets() {
        return ResponseEntity.ok(this.walletService.getWallets());
    }

    @GetMapping(path = "/{walletId}")
    ResponseEntity<GetWalletDetailsResponse> getWalletDetails(@PathVariable String walletId) {
        return ResponseEntity.ok(this.walletService.getWalletDetails(walletId));
    }
}