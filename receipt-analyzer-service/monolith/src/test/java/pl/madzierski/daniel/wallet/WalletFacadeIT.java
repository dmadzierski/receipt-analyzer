package pl.madzierski.daniel.wallet;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.madzierski.daniel.IntegrationTest;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;
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
        createUser("anna");
        UUID otherUserId = createUser("bartek");
        createWallet(otherUserId, "Bartek's wallet");

        UUID walletId = walletFacade.addWallet(new CreateWalletRequest("Home"), "anna").id();
        flush();

        assertThat(walletFacade.getWallets("anna").items())
            .extracting(GetWalletListResponse.GetWalletListResponseItem::id, GetWalletListResponse.GetWalletListResponseItem::name)
            .containsExactly(tuple(walletId, "Home"));
    }

    @Test
    void addingWalletForUnknownUserFails() {
        assertError(() -> walletFacade.addWallet(new CreateWalletRequest("Home"), "nobody"), AppRuntimeExceptionMessages.USER_NOT_FOUND);
    }

    @Test
    void walletDetailsShowCorrectRevisionAndStoreOfEachReceipt() {
        UUID walletId = createWallet(createUser("anna"), "Home");
        UUID storeId = createStore("Biedronka", "ul. Testowa 1", "Poznań");
        UUID receiptId = insertReceipt(walletId, storeId, "Weekly shopping");
        insertRevision(receiptId, "BIEDRONKA_JSON", "999.99", false, null);
        insertRevision(receiptId, "USER", "330.75", true, LocalDateTime.of(2026, 5, 22, 10, 4));
        flush();

        GetWalletDetailsResponse details = walletFacade.getWalletDetails(walletId);

        assertThat(details.name()).isEqualTo("Home");
        assertThat(details.receipts()).hasSize(1);
        GetWalletDetailsResponse.Receipt receipt = details.receipts().getFirst();
        assertThat(receipt.name()).isEqualTo("Weekly shopping");
        assertThat(receipt.totalPrice()).isEqualByComparingTo("330.75");
        assertThat(receipt.paymentDate()).isEqualTo(LocalDate.of(2026, 5, 22));
        assertThat(receipt.isCorrect()).isTrue();
        assertThat(receipt.store().getBrand()).isEqualTo("Biedronka");
        assertThat(receipt.store().getCity()).isEqualTo("Poznań");
    }

    @Test
    void walletDetailsListReceiptWithoutCorrectRevision() {
        UUID walletId = createWallet(createUser("anna"), "Home");
        UUID receiptId = insertReceipt(walletId, null, "Not checked yet");
        insertRevision(receiptId, "BIEDRONKA_JSON", "12.50", false, null);
        flush();

        GetWalletDetailsResponse.Receipt receipt = walletFacade.getWalletDetails(walletId).receipts().getFirst();

        assertThat(receipt.name()).isEqualTo("Not checked yet");
        assertThat(receipt.totalPrice()).isNull();
        assertThat(receipt.store()).isNull();
    }

    @Test
    void unknownWalletFails() {
        assertError(() -> walletFacade.getWalletDetails(UNKNOWN_ID), AppRuntimeExceptionMessages.WALLET_NOT_FOUND);
    }
}
