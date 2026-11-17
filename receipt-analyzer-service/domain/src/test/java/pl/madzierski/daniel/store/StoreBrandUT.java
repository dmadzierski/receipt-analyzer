package pl.madzierski.daniel.store;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import pl.madzierski.daniel.exception.AppIllegalStateException;
import pl.madzierski.daniel.exception.AppIllegalStateExceptionMessages;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StoreBrandUT {

    @Test
    void createdBrandHasTrimmedName() {
        assertThat(StoreBrand.create(" Biedronka  ").getName()).isEqualTo("Biedronka");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void brandNeedsName(String name) {
        assertThatThrownBy(() -> StoreBrand.create(name))
            .isInstanceOf(AppIllegalStateException.class)
            .hasFieldOrPropertyWithValue("type", AppIllegalStateExceptionMessages.BLANK_NAME);
    }
}
