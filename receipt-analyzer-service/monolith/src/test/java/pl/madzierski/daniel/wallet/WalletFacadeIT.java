package pl.madzierski.daniel.wallet;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.madzierski.daniel.IntegrationTest;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;
import pl.madzierski.daniel.user.model.AuthenticatedUser;
import pl.madzierski.daniel.wallet.model.CreateWalletRequest;
import pl.madzierski.daniel.wallet.model.GetWalletDetailsResponse;
import pl.madzierski.daniel.wallet.model.GetWalletListResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    void walletDetailsListEveryReceiptWithItsOwnStoreAndPreferredTotal() {
        UUID annaId = createUser("anna");
        UUID walletId = createWallet(annaId, "Home");
        UUID biedronkaId = createStore("Biedronka", "ul. Testowa 1", "Poznań");
        UUID lidlId = createStore("Lidl", "ul. Testowa 2", "Gniezno");
        UUID biedronkaReceiptId = insertReceipt(walletId, biedronkaId, "Biedronka");
        insertRevision(biedronkaReceiptId, "BIEDRONKA_JSON", "999.99", false, null);
        insertRevision(biedronkaReceiptId, "USER", "10.00", true, true, LocalDateTime.of(2026, 5, 22, 10, 4));
        insertRevision(insertReceipt(walletId, lidlId, "Lidl"), "USER", "20.00", true, true, null);
        insertRevision(insertReceipt(walletId, null, "Bez sklepu"), "USER", "30.00", false, true, null);
        insertReceipt(createWallet(annaId, "Other"), biedronkaId, "Other wallet");
        flush();

        GetWalletDetailsResponse details = walletFacade.getWalletDetails(new AuthenticatedUser(annaId), walletId);

        assertThat(details.name()).isEqualTo("Home");
        assertThat(details.receipts())
            .extracting(GetWalletDetailsResponse.Receipt::name, receipt -> receipt.totalPrice().toPlainString(), GetWalletDetailsResponse.Receipt::isCorrect,
                GetWalletDetailsResponse.Receipt::paymentDate,
                receipt -> receipt.store() != null ? receipt.store().getBrand() : null,
                receipt -> receipt.store() != null ? receipt.store().getCity() : null)
            .containsExactlyInAnyOrder(
                tuple("Biedronka", "10.00", true, LocalDate.of(2026, 5, 22), "Biedronka", "Poznań"),
                tuple("Lidl", "20.00", true, null, "Lidl", "Gniezno"),
                tuple("Bez sklepu", "30.00", false, null, null, null));
    }

    @Test
    void receiptWithManyCorrectRevisionsIsListedOnceWithPreferredRevision() {
        UUID annaId = createUser("anna");
        UUID walletId = createWallet(annaId, "Home");
        UUID receiptId = insertReceipt(walletId, null, "Weekly shopping");
        insertRevision(receiptId, "BIEDRONKA_JSON", "100.00", true, null);
        insertRevision(receiptId, "USER", "110.00", true, true, null);
        insertRevision(receiptId, "USER", "120.00", true, null);
        flush();

        GetWalletDetailsResponse details = walletFacade.getWalletDetails(new AuthenticatedUser(annaId), walletId);

        assertThat(details.receipts()).singleElement()
            .satisfies(receipt -> assertThat(receipt.totalPrice()).isEqualByComparingTo("110.00"));
    }

    @Test
    void walletDetailsListReceiptWithoutPreferredRevision() {
        UUID annaId = createUser("anna");
        UUID walletId = createWallet(annaId, "Home");
        UUID receiptId = insertReceipt(walletId, null, "Not checked yet");
        insertRevision(receiptId, "BIEDRONKA_JSON", "12.50", true, null);
        flush();

        GetWalletDetailsResponse.Receipt receipt = walletFacade.getWalletDetails(new AuthenticatedUser(annaId), walletId).receipts().getFirst();

        assertThat(receipt.name()).isEqualTo("Not checked yet");
        assertThat(receipt.totalPrice()).isNull();
        assertThat(receipt.store()).isNull();
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
