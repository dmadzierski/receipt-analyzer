package pl.madzierski.daniel.app.wallet;

import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.madzierski.daniel.app.wallet.model.CreateWalletRequest;
import pl.madzierski.daniel.app.wallet.model.CreateWalletResponse;
import pl.madzierski.daniel.app.wallet.model.GetWalletResponse;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping(path = "/wallets", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class WalletController {

    private final WalletService walletService;

    @PostMapping()
    public ResponseEntity<CreateWalletResponse> addWallet(@RequestBody CreateWalletRequest request) {
        return ResponseEntity.ok(this.walletService.addWallet(request));
    }

    @GetMapping()
    public ResponseEntity<List<GetWalletResponse>> getWallets(){
        return ResponseEntity.ok(this.walletService.getWallets());
    }
}
