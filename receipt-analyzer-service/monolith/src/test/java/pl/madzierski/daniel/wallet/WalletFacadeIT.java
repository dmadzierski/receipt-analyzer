package pl.madzierski.daniel.wallet;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.madzierski.daniel.IntegrationTest;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;
import pl.madzierski.daniel.user.model.AuthenticatedUser;
import pl.madzierski.daniel.wallet.model.CreateWalletRequest;
import pl.madzierski.daniel.wallet.model.GetWalletDetailsResponse;
import pl.madzierski.daniel.wallet.model.GetWalletListResponse;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class WalletFacadeIT extends IntegrationTest {

    @Autowired
    private WalletFacade walletFacade;

    @Test
    void listsOnlyWalletsOfCurrentUser() {
        AuthenticatedUser anna = new AuthenticatedUser(createUser("anna"));
        UUID otherUserId = createUser("bartek");
        createWallet(otherUserId, "Bartek's wallet");

        UUID walletId = walletFacade.addWallet(anna, new CreateWalletRequest("Home")).id();
        flush();

        assertThat(walletFacade.getWallets(anna).items())
            .extracting(GetWalletListResponse.GetWalletListResponseItem::id, GetWalletListResponse.GetWalletListResponseItem::name)
            .containsExactly(tuple(walletId, "Home"));
    }

    @Test
    void walletDetailsReturnNameOfOwnWallet() {
        UUID annaId = createUser("anna");
        UUID walletId = createWallet(annaId, "Home");
        flush();

        GetWalletDetailsResponse details = walletFacade.getWalletDetails(new AuthenticatedUser(annaId), walletId);

        assertThat(details.id()).isEqualTo(walletId);
        assertThat(details.name()).isEqualTo("Home");
    }

    @Test
    void otherUsersWalletIsReportedAsUnknown() {
        UUID walletId = createWallet(createUser("anna"), "Home");
        insertReceipt(walletId, null, "Anna's receipt");
        AuthenticatedUser bartek = new AuthenticatedUser(createUser("bartek"));
        flush();

        assertError(() -> walletFacade.getWalletDetails(bartek, walletId), AppRuntimeExceptionMessages.WALLET_NOT_FOUND);
        assertThat(walletFacade.getWallets(bartek).items()).isEmpty();
    }

    @Test
    void unknownWalletFails() {
        assertError(() -> walletFacade.getWalletDetails(new AuthenticatedUser(UNKNOWN_ID), UNKNOWN_ID), AppRuntimeExceptionMessages.WALLET_NOT_FOUND);
    }
}
