package pl.madzierski.daniel.wallet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import pl.madzierski.daniel.exception.AppIllegalStateException;
import pl.madzierski.daniel.exception.AppIllegalStateExceptionMessages;
import pl.madzierski.daniel.user.model.UserQuery;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WalletUT {

    @Test
    void createdWalletHasTrimmedNameAndOwner() {
        UserQuery owner = new UserQuery(UUID.randomUUID());

        Wallet wallet = Wallet.create("  Home ", owner);

        assertThat(wallet.getId()).isNull();
        assertThat(wallet.getName()).isEqualTo("Home");
        assertThat(wallet.getOwner()).isEqualTo(owner);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void walletNeedsName(String name) {
        assertThatThrownBy(() -> Wallet.create(name, new UserQuery(UUID.randomUUID())))
            .isInstanceOf(AppIllegalStateException.class)
            .hasFieldOrPropertyWithValue("type", AppIllegalStateExceptionMessages.BLANK_NAME);
    }
}
