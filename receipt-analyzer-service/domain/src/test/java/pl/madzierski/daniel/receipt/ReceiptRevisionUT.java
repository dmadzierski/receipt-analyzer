package pl.madzierski.daniel.receipt;

import org.junit.jupiter.api.Test;
import pl.madzierski.daniel.exception.AppIllegalStateException;
import pl.madzierski.daniel.exception.AppIllegalStateExceptionMessages;
import pl.madzierski.daniel.file_group.model.FileGroupQuery;
import pl.madzierski.daniel.product_dict.model.ProductDictQuery;
import pl.madzierski.daniel.receipt.model.ReceiptItemQuery;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pl.madzierski.daniel.receipt.ReceiptResolverStrategyType.BIEDRONKA;
import static pl.madzierski.daniel.receipt.ReceiptResolverStrategyType.BIEDRONKA_JSON;
import static pl.madzierski.daniel.receipt.ReceiptResolverStrategyType.USER;

class ReceiptRevisionUT {

    private static final ProductDictQuery ONION = new ProductDictQuery(UUID.randomUUID());
    private static final ProductDictQuery MILK = new ProductDictQuery(UUID.randomUUID());

    @Test
    void digitizedRevisionIsNotPreferredAndIsCorrectWhenPricesAddUp() {
        Receipt receipt = receipt();
        FileGroupQuery sourceFileGroup = new FileGroupQuery(UUID.randomUUID());

        ReceiptRevision revision = ReceiptRevision.createDigitized("1.0", BIEDRONKA_JSON, dec("5.00"), null,
            List.of(item("Cebula", "2", "2.50", "5.00")), receipt, sourceFileGroup);

        assertThat(revision.getIsPreferredRevision()).isFalse();
        assertThat(revision.getIsCorrect()).isTrue();
        assertThat(revision.getResolver()).isEqualTo(BIEDRONKA_JSON);
        assertThat(revision.getReceipt()).isSameAs(receipt);
        assertThat(revision.getSourceFileGroup()).isEqualTo(sourceFileGroup);
        assertThat(revision.getItems()).extracting(ReceiptItem::getName).containsExactly("Cebula");
    }

    @Test
    void digitizedRevisionIsIncorrectWhenItemsDoNotAddUpToTotal() {
        assertThat(digitized("6.00", item("Cebula", "2", "2.50", "5.00")).getIsCorrect()).isFalse();
    }

    @Test
    void digitizedRevisionIsIncorrectWithoutItemsOrTotal() {
        assertThat(digitized("1.00").getIsCorrect()).isFalse();
        assertThat(digitized(null, item("Cebula", "1", "1.00", "1.00")).getIsCorrect()).isFalse();
    }

    @Test
    void digitizedRevisionIsIncorrectWhenAnyItemIsInconsistentEvenIfSumMatches() {
        ReceiptRevision revision = digitized("3.50",
            item("Cebula", "1", "2.00", "2.00"),
            item("Mleko", "1", "1.00", "1.50"));

        assertThat(revision.getIsCorrect()).isFalse();
    }

    @Test
    void itemTotalIsAmountTimesUnitPriceMinusDiscount() {
        assertThat(digitized("2.50", pricedItem("2", "1.50", "0.50", "2.50")).getIsCorrect()).isTrue();
        assertThat(digitized("2.99", pricedItem("2", "1.50", null, "2.99")).getIsCorrect()).isFalse();
    }

    @Test
    void itemLineValueIsRoundedToCentsBeforeDiscountIsSubtracted() {
        assertThat(digitized("1.00", pricedItem("0.333", "3.00", null, "1.00")).getIsCorrect()).isTrue();
    }

    @Test
    void itemWithoutAmountUnitPriceOrTotalMakesRevisionIncorrect() {
        assertThat(digitized("1.50", pricedItem(null, "1.50", null, "1.50")).getIsCorrect()).isFalse();
        assertThat(digitized("1.50", pricedItem("1", null, null, "1.50")).getIsCorrect()).isFalse();
        assertThat(digitized("0.00", pricedItem("1", "1.50", null, null)).getIsCorrect()).isFalse();
    }

    @Test
    void copyIsNotPreferredUserRevisionWithCopiedItemsPointingToOriginal() {
        UUID originalId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        Receipt receipt = receipt();
        FileGroupQuery sourceFileGroup = new FileGroupQuery(UUID.randomUUID());
        LocalDateTime paymentDate = LocalDateTime.of(2026, 5, 22, 10, 0);
        ReceiptRevision original = ReceiptRevision.restore(originalId, null, null, "1.0", BIEDRONKA_JSON, dec("1.90"),
            paymentDate, true, true, receipt, null,
            List.of(restoredItem(itemId, "Cebula", ONION, "1", "2.00", "0.10", "1.90", 3)), sourceFileGroup);

        ReceiptRevision copy = original.copyAsUserRevision();

        assertThat(copy.getId()).isNull();
        assertThat(copy.getResolver()).isEqualTo(USER);
        assertThat(copy.getIsPreferredRevision()).isFalse();
        assertThat(copy.getParentReceiptRevision().getId()).isEqualTo(originalId);
        assertThat(copy.getReceipt()).isSameAs(receipt);
        assertThat(copy.getSourceFileGroup()).isEqualTo(sourceFileGroup);
        assertThat(copy.getTotalPrice()).isEqualTo(dec("1.90"));
        assertThat(copy.getPaymentDate()).isEqualTo(paymentDate);
        assertThat(copy.getIsCorrect()).isTrue();
        assertThat(copy.getItems()).singleElement().satisfies(item -> {
            assertThat(item.getId()).isNull();
            assertThat(item.getParentItem().getId()).isEqualTo(itemId);
            assertThat(item)
                .extracting(ReceiptItem::getName, ReceiptItem::getNameDict, ReceiptItem::getAmount, ReceiptItem::getUnitPrice,
                    ReceiptItem::getDiscount, ReceiptItem::getTotalPrice, ReceiptItem::getPosition)
                .containsExactly("Cebula", ONION, dec("1"), dec("2.00"), dec("0.10"), dec("1.90"), 3);
        });
    }

    @Test
    void updateDetailsChangesOnlyProvidedValues() {
        LocalDateTime newDate = LocalDateTime.of(2026, 6, 1, 12, 0);
        ReceiptRevision revision = ReceiptRevision.restore(null, null, null, null, USER, dec("1.00"),
            LocalDateTime.of(2026, 5, 1, 12, 0), false, false, null, null, List.of(), null);

        revision.updateDetails(null, newDate, true, null);

        assertThat(revision.getTotalPrice()).isEqualTo(dec("1.00"));
        assertThat(revision.getPaymentDate()).isEqualTo(newDate);
        assertThat(revision.getIsPreferredRevision()).isTrue();
        assertThat(revision.getIsCorrect()).isFalse();
    }

    @Test
    void updateDetailsMarksRevisionCorrectWhenTotalMatchesItems() {
        ReceiptRevision revision = userRevision("9.99", false, item("Cebula", "1", "1.00", "1.00"));

        revision.updateDetails(dec("1.00"), null, null, null);

        assertThat(revision.getIsCorrect()).isTrue();
    }

    @Test
    void updateItemsMarksRevisionCorrectWhenItemsMatchTotal() {
        UUID itemId = UUID.randomUUID();
        ReceiptRevision revision = userRevision("2.00", false,
            restoredItem(itemId, "Cebula", null, "1", "1.00", null, "1.00", 1));

        revision.updateItems(List.of(change(itemId, "Cebula", null, "2", "1.00", null, "2.00", 1)));

        assertThat(revision.getIsCorrect()).isTrue();
    }

    @Test
    void updatesNeverClearCorrectFlagWhenPricesAreInconsistent() {
        ReceiptRevision revision = userRevision("9.99", true, item("Cebula", "1", "1.00", "1.00"));

        revision.updateDetails(null, null, null, null);
        revision.updateItems(List.of(change(null, "Mleko", null, "1", "2.00", null, "2.00", 1)));

        assertThat(revision.getIsCorrect()).isTrue();
    }

    @Test
    void updateItemsRemovesMissingUpdatesExistingAndAddsNewItems() {
        UUID keptId = UUID.randomUUID();
        UUID removedId = UUID.randomUUID();
        ReceiptRevision revision = userRevision(null, false,
            restoredItem(keptId, "Old", null, "1", "1.00", null, "1.00", 1),
            restoredItem(removedId, "Removed", null, "1", "1.00", null, "1.00", 2));

        revision.updateItems(List.of(
            change(keptId, "Updated", ONION, "2", "1.00", "0.10", "1.90", 5),
            change(null, "Added", null, "1", "3.00", null, "3.00", 2)));

        assertThat(revision.getItems()).extracting(ReceiptItem::getName).containsExactlyInAnyOrder("Updated", "Added");
        assertThat(itemNamed(revision, "Updated"))
            .extracting(ReceiptItem::getId, ReceiptItem::getNameDict, ReceiptItem::getAmount, ReceiptItem::getUnitPrice,
                ReceiptItem::getDiscount, ReceiptItem::getTotalPrice, ReceiptItem::getPosition)
            .containsExactly(keptId, ONION, dec("2"), dec("1.00"), dec("0.10"), dec("1.90"), 5);
        assertThat(itemNamed(revision, "Added").getId()).isNull();
    }

    @Test
    void updateItemsIgnoresUnknownItemIds() {
        UUID existingId = UUID.randomUUID();
        ReceiptRevision revision = userRevision(null, false, restoredItem(existingId, "Cebula", null, null, null, null, null, 1));

        revision.updateItems(List.of(
            change(existingId, "Cebula", null, null, null, null, null, 1),
            change(UUID.randomUUID(), "Ghost", null, null, null, null, null, 2)));

        assertThat(revision.getItems()).extracting(ReceiptItem::getName).containsExactly("Cebula");
    }

    @Test
    void onlyUserRevisionsHaveEditableItems() {
        assertThat(revision(USER).canEditItems()).isTrue();
        assertThat(revision(BIEDRONKA_JSON).canEditItems()).isFalse();
        assertThat(revision(BIEDRONKA).canEditItems()).isFalse();
    }

    @Test
    void itemsOfMachineRevisionCannotBeReplacedOrRenamed() {
        ReceiptRevision revision = revision(BIEDRONKA_JSON, item("Cebula", "1", "1.00", "1.00"));

        assertThatThrownBy(() -> revision.updateItems(List.of())).isInstanceOf(AppIllegalStateException.class)
            .hasFieldOrPropertyWithValue("type", AppIllegalStateExceptionMessages.REVISION_NOT_USER_RESOLVER);
        assertThatThrownBy(() -> revision.renameItemsToProductNames(product -> Optional.of("Cebula")))
            .isInstanceOf(AppIllegalStateException.class)
            .hasFieldOrPropertyWithValue("type", AppIllegalStateExceptionMessages.REVISION_NOT_USER_RESOLVER);
        assertThat(revision.getItems()).extracting(ReceiptItem::getName).containsExactly("Cebula");
    }

    @Test
    void assignProductsSetsProductOnlyForMatchedItems() {
        ReceiptRevision revision = revision(BIEDRONKA_JSON,
            item("Cebula", "1", "1.00", "1.00"),
            item("Mleko", "1", "1.00", "1.00"));

        revision.assignProducts(names -> Map.of("Cebula", ONION));

        assertThat(itemNamed(revision, "Cebula").getNameDict()).isEqualTo(ONION);
        assertThat(itemNamed(revision, "Mleko").getNameDict()).isNull();
    }

    @Test
    void assignProductsKeepsAlreadyAssignedProduct() {
        ReceiptRevision revision = revision(BIEDRONKA_JSON,
            restoredItem(UUID.randomUUID(), "Cebula", MILK, "1", "1.00", null, "1.00", 1),
            item("Mleko", "1", "1.00", "1.00"));
        List<Set<String>> requestedNames = new ArrayList<>();

        revision.assignProducts(names -> {
            requestedNames.add(names);
            return Map.of("Cebula", ONION, "Mleko", MILK);
        });

        assertThat(requestedNames).containsExactly(Set.of("Mleko"));
        assertThat(itemNamed(revision, "Cebula").getNameDict()).isEqualTo(MILK);
        assertThat(itemNamed(revision, "Mleko").getNameDict()).isEqualTo(MILK);
    }

    @Test
    void assignProductsToNewItemsSkipsExistingItemsAndItemsWithProduct() {
        UUID existingId = UUID.randomUUID();
        ReceiptRevision revision = userRevision(null, false,
            restoredItem(existingId, "Existing", null, "1", "1.00", null, "1.00", 1));
        revision.updateItems(List.of(
            change(existingId, "Existing", null, "1", "1.00", null, "1.00", 1),
            change(null, "New", null, "1", "1.00", null, "1.00", 2),
            change(null, "Chosen", MILK, "1", "1.00", null, "1.00", 3)));
        List<Set<String>> requestedNames = new ArrayList<>();

        revision.assignProductsToNewItems(names -> {
            requestedNames.add(names);
            return Map.of("Existing", ONION, "New", ONION, "Chosen", ONION);
        });

        assertThat(requestedNames).containsExactly(Set.of("New"));
        assertThat(itemNamed(revision, "Existing").getNameDict()).isNull();
        assertThat(itemNamed(revision, "New").getNameDict()).isEqualTo(ONION);
        assertThat(itemNamed(revision, "Chosen").getNameDict()).isEqualTo(MILK);
    }

    @Test
    void itemCorrectionsPairUserItemsWithOriginalItemsWithoutProductAndKeepChosenProduct() {
        UUID onionId = UUID.randomUUID();
        UUID milkId = UUID.randomUUID();
        UUID breadId = UUID.randomUUID();
        ReceiptRevision original = savedRevision(BIEDRONKA_JSON,
            restoredItem(onionId, "CEB ZOL", null, "1", "1.00", null, "1.00", 1),
            restoredItem(milkId, "MLK 3,2", MILK, "1", "1.00", null, "1.00", 2),
            restoredItem(breadId, "CHLEB", null, "1", "1.00", null, "1.00", 3));
        ReceiptRevision copy = original.copyAsUserRevision();
        copy.assignProducts(names -> Map.of("CHLEB", ONION));

        List<ItemCorrection> corrections = copy.itemCorrections(original);

        assertThat(corrections).containsExactlyInAnyOrder(
            new ItemCorrection(onionId, "CEB ZOL", "CEB ZOL", null),
            new ItemCorrection(breadId, "CHLEB", "CHLEB", ONION));
    }

    @Test
    void itemCorrectionsAreRejectedForRevisionOtherThanParent() {
        ReceiptRevision original = savedRevision(BIEDRONKA_JSON, restoredItem(UUID.randomUUID(), "CEB ZOL", null, "1", "1.00", null, "1.00", 1));
        ReceiptRevision other = savedRevision(BIEDRONKA_JSON, restoredItem(UUID.randomUUID(), "CEB ZOL", null, "1", "1.00", null, "1.00", 1));
        ReceiptRevision copy = original.copyAsUserRevision();

        assertThatThrownBy(() -> copy.itemCorrections(other)).isInstanceOf(AppIllegalStateException.class)
            .hasFieldOrPropertyWithValue("type", AppIllegalStateExceptionMessages.REVISION_NOT_PARENT);
    }

    @Test
    void userRevisionWithoutParentCannotProvideItemCorrections() {
        ReceiptRevision userRevision = userRevision(null, false);

        assertThatThrownBy(() -> userRevision.itemCorrections(savedRevision(BIEDRONKA_JSON)))
            .isInstanceOf(AppIllegalStateException.class)
            .hasFieldOrPropertyWithValue("type", AppIllegalStateExceptionMessages.REVISION_NOT_PARENT);
    }

    @Test
    void machineRevisionCannotProvideItemCorrections() {
        ReceiptRevision original = revision(BIEDRONKA_JSON, restoredItem(UUID.randomUUID(), "CEB ZOL", null, "1", "1.00", null, "1.00", 1));

        assertThatThrownBy(() -> original.itemCorrections(original)).isInstanceOf(AppIllegalStateException.class)
            .hasFieldOrPropertyWithValue("type", AppIllegalStateExceptionMessages.REVISION_NOT_USER_RESOLVER);
    }

    @Test
    void assignProductsByItemIdSetsProductOnlyForListedItems() {
        UUID onionId = UUID.randomUUID();
        UUID milkId = UUID.randomUUID();
        ReceiptRevision revision = revision(BIEDRONKA_JSON,
            restoredItem(onionId, "Cebula", null, "1", "1.00", null, "1.00", 1),
            restoredItem(milkId, "Mleko", null, "1", "1.00", null, "1.00", 2));

        revision.assignProductsByItemId(Map.of(onionId, ONION));

        assertThat(itemNamed(revision, "Cebula").getNameDict()).isEqualTo(ONION);
        assertThat(itemNamed(revision, "Mleko").getNameDict()).isNull();
    }

    @Test
    void assignProductsByParentItemIdSetsProductOnlyForCopiesOfListedItemsWithoutProduct() {
        UUID onionId = UUID.randomUUID();
        UUID milkId = UUID.randomUUID();
        UUID breadId = UUID.randomUUID();
        ReceiptRevision revision = revision(USER,
            copiedItem("Cebula", null, onionId),
            copiedItem("Mleko", null, milkId),
            copiedItem("Chleb", MILK, breadId),
            item("Nowa", "1", "1.00", "1.00"));

        revision.assignProductsByParentItemId(Map.of(onionId, ONION, breadId, ONION));

        assertThat(itemNamed(revision, "Cebula").getNameDict()).isEqualTo(ONION);
        assertThat(itemNamed(revision, "Mleko").getNameDict()).isNull();
        assertThat(itemNamed(revision, "Chleb").getNameDict()).isEqualTo(MILK);
        assertThat(itemNamed(revision, "Nowa").getNameDict()).isNull();
    }

    @Test
    void renameItemsToProductNamesRenamesOnlyItemsWithKnownProduct() {
        ReceiptRevision revision = revision(USER,
            ReceiptItem.create("CEB ZOL", ONION, null, null, null, null, 1),
            ReceiptItem.create("MLK 3,2", MILK, null, null, null, null, 2),
            ReceiptItem.create("Bez produktu", null, null, null, null, null, 3));

        revision.renameItemsToProductNames(product -> product.equals(ONION) ? Optional.of("Cebula") : Optional.empty());

        assertThat(revision.getItems()).extracting(ReceiptItem::getName)
            .containsExactlyInAnyOrder("Cebula", "MLK 3,2", "Bez produktu");
    }

    @Test
    void isPreferredTreatsMissingFlagAsNotPreferred() {
        assertThat(restoredWithPreferred(null).isPreferred()).isFalse();
        assertThat(restoredWithPreferred(true).isPreferred()).isTrue();
    }

    @Test
    void itemsCannotBeModifiedFromOutside() {
        ReceiptRevision revision = revision(USER);

        assertThatThrownBy(() -> revision.getItems().add(item("Cebula", "1", "1.00", "1.00")))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    private static Receipt receipt() {
        return Receipt.restore(UUID.randomUUID(), null, null, "Paragon", null, null, null, null);
    }

    private static ReceiptRevision digitized(String totalPrice, ReceiptItem... items) {
        return ReceiptRevision.createDigitized("1.0", BIEDRONKA_JSON, dec(totalPrice), null, List.of(items), null, null);
    }

    private static ReceiptRevision userRevision(String totalPrice, boolean isCorrect, ReceiptItem... items) {
        return ReceiptRevision.restore(null, null, null, null, USER, dec(totalPrice), null, false, isCorrect, null, null,
            List.of(items), null);
    }

    private static ReceiptRevision savedRevision(ReceiptResolverStrategyType resolver, ReceiptItem... items) {
        return ReceiptRevision.restore(UUID.randomUUID(), null, null, null, resolver, null, null, false, false, null, null,
            List.of(items), null);
    }

    private static ReceiptRevision revision(ReceiptResolverStrategyType resolver, ReceiptItem... items) {
        return ReceiptRevision.restore(null, null, null, null, resolver, null, null, false, false, null, null,
            List.of(items), null);
    }

    private static ReceiptRevision restoredWithPreferred(Boolean isPreferredRevision) {
        return ReceiptRevision.restore(null, null, null, null, USER, null, null, isPreferredRevision, false, null, null,
            List.of(), null);
    }

    private static ReceiptItem restoredItem(UUID id, String name, ProductDictQuery nameDict, String amount, String unitPrice,
                                            String discount, String totalPrice, Integer position) {
        return ReceiptItem.restore(id, null, null, name, nameDict, dec(amount), dec(unitPrice), dec(discount),
            dec(totalPrice), position, null);
    }

    private static ReceiptItemChange change(UUID id, String name, ProductDictQuery nameDict, String amount,
                                            String unitPrice, String discount, String totalPrice, Integer position) {
        return new ReceiptItemChange(id, name, nameDict, dec(amount), dec(unitPrice), dec(discount), dec(totalPrice),
            position);
    }

    private static ReceiptItem copiedItem(String name, ProductDictQuery nameDict, UUID parentItemId) {
        return ReceiptItem.restore(UUID.randomUUID(), null, null, name, nameDict, dec("1"), dec("1.00"), null,
            dec("1.00"), 1, new ReceiptItemQuery(parentItemId));
    }

    private static ReceiptItem pricedItem(String amount, String unitPrice, String discount, String totalPrice) {
        return ReceiptItem.create("Item", null, dec(amount), dec(unitPrice), dec(discount), dec(totalPrice), 1);
    }

    private static ReceiptItem item(String name, String amount, String unitPrice, String totalPrice) {
        return ReceiptItem.create(name, null, dec(amount), dec(unitPrice), null, dec(totalPrice), 1);
    }

    private static ReceiptItem itemNamed(ReceiptRevision revision, String name) {
        return revision.getItems().stream()
            .filter(item -> item.getName().equals(name))
            .findFirst()
            .orElseThrow();
    }

    private static BigDecimal dec(String value) {
        return value == null ? null : new BigDecimal(value);
    }
}
