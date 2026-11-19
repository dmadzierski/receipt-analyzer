package pl.madzierski.daniel.receipt;

import org.junit.jupiter.api.Test;
import pl.madzierski.daniel.exception.AppIllegalStateException;
import pl.madzierski.daniel.exception.AppIllegalStateExceptionMessages;
import pl.madzierski.daniel.receipt.model.ReceiptQuery;
import pl.madzierski.daniel.receipt.model.ReceiptRevisionQuery;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    void newReceiptHasNoPreferredRevision() {
        assertThat(Receipt.create("Paragon", null, null, null, null).getPreferredRevision()).isNull();
    }

    @Test
    void choosingPreferredRevisionReplacesPreviousOne() {
        Receipt receipt = receipt();
        ReceiptRevision first = revisionOf(receipt);
        ReceiptRevision second = revisionOf(receipt);

        receipt.choosePreferredRevision(first);
        receipt.choosePreferredRevision(second);

        assertThat(receipt.getPreferredRevision()).isEqualTo(new ReceiptRevisionQuery(second.getId()));
        assertThat(receipt.isPreferred(first)).isFalse();
        assertThat(receipt.isPreferred(second)).isTrue();
    }

    @Test
    void choosingAlreadyPreferredRevisionKeepsIt() {
        Receipt receipt = receipt();
        ReceiptRevision revision = revisionOf(receipt);
        receipt.choosePreferredRevision(revision);

        receipt.choosePreferredRevision(revision);

        assertThat(receipt.isPreferred(revision)).isTrue();
    }

    @Test
    void unmarkingPreferredRevisionLeavesReceiptWithoutPreferredRevision() {
        Receipt receipt = receipt();
        ReceiptRevision revision = revisionOf(receipt);
        receipt.choosePreferredRevision(revision);

        receipt.unmarkPreferredRevision(revision);

        assertThat(receipt.getPreferredRevision()).isNull();
    }

    @Test
    void unmarkingOtherRevisionKeepsPreferredRevision() {
        Receipt receipt = receipt();
        ReceiptRevision preferred = revisionOf(receipt);
        ReceiptRevision other = revisionOf(receipt);
        receipt.choosePreferredRevision(preferred);

        receipt.unmarkPreferredRevision(other);

        assertThat(receipt.isPreferred(preferred)).isTrue();
    }

    @Test
    void revisionOfAnotherReceiptCannotBePreferredOrUnmarked() {
        Receipt receipt = receipt();
        ReceiptRevision preferred = revisionOf(receipt);
        receipt.choosePreferredRevision(preferred);
        ReceiptRevision foreign = revisionOf(receipt());

        assertThatThrownBy(() -> receipt.choosePreferredRevision(foreign))
            .isInstanceOf(AppIllegalStateException.class)
            .hasFieldOrPropertyWithValue("type", AppIllegalStateExceptionMessages.REVISION_NOT_OF_RECEIPT);
        assertThatThrownBy(() -> receipt.unmarkPreferredRevision(foreign))
            .isInstanceOf(AppIllegalStateException.class)
            .hasFieldOrPropertyWithValue("type", AppIllegalStateExceptionMessages.REVISION_NOT_OF_RECEIPT);
        assertThat(receipt.isPreferred(preferred)).isTrue();
    }

    private static Receipt receipt() {
        return Receipt.restore(UUID.randomUUID(), null, null, null, "Paragon", null, null, null, null, null);
    }

    private static ReceiptRevision revisionOf(Receipt receipt) {
        return ReceiptRevision.restore(UUID.randomUUID(), null, null, null, null, ReceiptResolverStrategyType.USER, null, null,
            false, new ReceiptQuery(receipt.getId()), null, List.of(), null);
    }
}
