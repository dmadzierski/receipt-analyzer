package pl.madzierski.daniel.receipt;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ReceiptUT {

    @Test
    void createKeepsGivenName() {
        assertThat(Receipt.create("Biedronka 22.05", null, null, null, null).getName()).isEqualTo("Biedronka 22.05");
    }

    @Test
    void createUsesCurrentDateTimeWhenNameIsMissingOrBlank() {
        LocalDateTime before = LocalDateTime.now();

        String missing = Receipt.create(null, null, null, null, null).getName();
        String blank = Receipt.create("   ", null, null, null, null).getName();

        assertThat(LocalDateTime.parse(missing)).isBetween(before, LocalDateTime.now());
        assertThat(LocalDateTime.parse(blank)).isBetween(before, LocalDateTime.now());
    }
}
