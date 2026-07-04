package pl.madzierski.daniel.app.wallet;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.madzierski.daniel.app.wallet.model.CreateWalletRequest;
import pl.madzierski.daniel.app.wallet.model.CreateWalletResponse;
import pl.madzierski.daniel.app.wallet.model.GetWalletResponse;
import pl.madzierski.daniel.security.SecurityUtils;

import java.util.List;

@Service
@AllArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;

    public CreateWalletResponse addWallet(CreateWalletRequest request) {
        WalletEntity savedWallet = walletRepository.save(WalletEntity.builder().name(request.name()).userSub(SecurityUtils.getCurrentUserSub()).build());
        return new CreateWalletResponse(savedWallet.getId(), savedWallet.getName());
    }

    public List<GetWalletResponse> getWallets() {
        return walletRepository.findAllByUserSub(SecurityUtils.getCurrentUserSub()).stream().map(wallet -> new GetWalletResponse(wallet.getId(), wallet.getName())).toList();
    }
}
