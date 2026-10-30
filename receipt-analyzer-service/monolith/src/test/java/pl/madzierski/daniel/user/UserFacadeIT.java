package pl.madzierski.daniel.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.madzierski.daniel.IntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

class UserFacadeIT extends IntegrationTest {

    @Autowired
    private UserFacade userFacade;

    @Test
    void syncCreatesUserOnFirstLoginOnly() {
        userFacade.syncMe("keycloak-sub-1");
        flush();
        userFacade.syncMe("keycloak-sub-1");
        flush();

        Integer users = jdbcTemplate.queryForObject("SELECT count(*) FROM app_user WHERE user_sub = ?", Integer.class, "keycloak-sub-1");
        assertThat(users).isEqualTo(1);
    }
}
