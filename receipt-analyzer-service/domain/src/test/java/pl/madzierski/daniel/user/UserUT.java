package pl.madzierski.daniel.user;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserUT {

    @Test
    void registeredUserHasOnlySubject() {
        User user = User.register("keycloak-sub");

        assertThat(user.getId()).isNull();
        assertThat(user.getUserSub()).isEqualTo("keycloak-sub");
    }
}
