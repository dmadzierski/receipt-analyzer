package pl.madzierski.daniel.receipt;

import lombok.*;
import pl.madzierski.daniel.digitization.ReceiptResolverStrategyType;
import pl.madzierski.daniel.exception.AppIllegalStateException;
import pl.madzierski.daniel.exception.AppIllegalStateExceptionMessages;
import pl.madzierski.daniel.file_group.model.FileGroupQuery;
import pl.madzierski.daniel.product_dict.model.ProductDictQuery;
import pl.madzierski.daniel.receipt.model.ReceiptQuery;
import pl.madzierski.daniel.receipt.model.ReceiptRevisionQuery;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(exclude = {"items", "receipt", "sourceFileGroup"})
class ReceiptRevision {
    private UUID id;
    private Long version;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String revision;
    private ReceiptResolverStrategyType resolver;
    private BigDecimal totalPrice;
    private LocalDateTime paymentDate;
    private Boolean isCorrect;
    private ReceiptQuery receipt;
    private ReceiptRevisionQuery parentReceiptRevision;
    @Builder.Default
    private Set<ReceiptItem> items = new HashSet<>();
    private FileGroupQuery sourceFileGroup;

    static ReceiptRevision createDigitized(String revision, ReceiptResolverStrategyType resolver, BigDecimal totalPrice,
                                           LocalDateTime paymentDate, Collection<ReceiptItem> items, ReceiptQuery receipt,
                                           FileGroupQuery sourceFileGroup) {
        ReceiptRevision created = builder()
            .revision(revision)
            .resolver(resolver)
            .totalPrice(totalPrice)
            .paymentDate(paymentDate)
            .receipt(receipt)
            .sourceFileGroup(sourceFileGroup)
            .items(new HashSet<>(items))
            .build();
        created.isCorrect = created.isPriceConsistent();
        return created;
    }

    static ReceiptRevision restore(UUID id, Long version, LocalDateTime createdDate, LocalDateTime modifiedDate, String revision,
                                   ReceiptResolverStrategyType resolver, BigDecimal totalPrice, LocalDateTime paymentDate,
                                   Boolean isCorrect, ReceiptQuery receipt,
                                   ReceiptRevisionQuery parentReceiptRevision, Collection<ReceiptItem> items,
                                   FileGroupQuery sourceFileGroup) {
        return builder()
            .id(id)
            .version(version)
            .createdDate(createdDate)
            .modifiedDate(modifiedDate)
            .revision(revision)
            .resolver(resolver)
            .totalPrice(totalPrice)
            .paymentDate(paymentDate)
            .isCorrect(isCorrect)
            .receipt(receipt)
            .parentReceiptRevision(parentReceiptRevision)
            .items(new HashSet<>(items))
            .sourceFileGroup(sourceFileGroup)
            .build();
    }

    ReceiptRevision copyAsUserRevision() {
        return builder()
            .resolver(ReceiptResolverStrategyType.USER)
            .totalPrice(totalPrice)
            .paymentDate(paymentDate)
            .isCorrect(isCorrect)
            .receipt(receipt)
            .parentReceiptRevision(new ReceiptRevisionQuery(id))
            .sourceFileGroup(sourceFileGroup)
            .items(items.stream()
                .map(ReceiptItem::copy)
                .collect(Collectors.toCollection(HashSet::new)))
            .build();
    }

    public Set<ReceiptItem> getItems() {
        return Collections.unmodifiableSet(items);
    }

    boolean canEditItems() {
        return resolver == ReceiptResolverStrategyType.USER;
    }

    void changeSummary(BigDecimal totalPrice, LocalDateTime paymentDate) {
        this.totalPrice = totalPrice;
        this.paymentDate = paymentDate;
        markCorrectIfPricesConsistent();
    }

    void markCorrect(boolean correct) {
        this.isCorrect = correct;
        markCorrectIfPricesConsistent();
    }

    void updateItems(Collection<ReceiptItemChange> itemChanges) {
        ensureCanEditItems();
        Map<UUID, ReceiptItemChange> receiptItemChangeMap = itemChanges.stream()
            .filter(change -> change.id() != null)
            .collect(Collectors.toMap(ReceiptItemChange::id, Function.identity(), (first, second) -> second));
        items.removeIf(item -> !receiptItemChangeMap.containsKey(item.getId()));
        items.forEach(item -> item.apply(receiptItemChangeMap.get(item.getId())));
        itemChanges.stream()
            .filter(change -> change.id() == null)
            .map(ReceiptItem::create)
            .forEach(items::add);
        markCorrectIfPricesConsistent();
    }

    void assignProducts(Function<Set<String>, Map<String, ProductDictQuery>> findProducts) {
        assignProducts(items, findProducts);
    }

    void assignProductsToNewItems(Function<Set<String>, Map<String, ProductDictQuery>> findProducts) {
        assignProducts(items.stream()
            .filter(item -> item.getId() == null)
            .toList(), findProducts);
    }

    private void assignProducts(Collection<ReceiptItem> itemsToAssign,
                                Function<Set<String>, Map<String, ProductDictQuery>> findProducts) {
        List<ReceiptItem> itemWithoutProduct = itemsToAssign.stream()
            .filter(item -> item.getNameDict() == null)
            .toList();
        if (itemWithoutProduct.isEmpty()) return;
        Map<String, ProductDictQuery> productsByName = findProducts.apply(itemWithoutProduct.stream()
            .map(ReceiptItem::getName)
            .collect(Collectors.toSet()));
        itemWithoutProduct.forEach(item -> Optional.ofNullable(productsByName.get(item.getName())).ifPresent(item::assignProduct));
    }

    List<ItemCorrection> itemCorrections(ReceiptRevision original) {
        ensureCanEditItems();
        if (parentReceiptRevision == null || !parentReceiptRevision.id().equals(original.id))
            throw new AppIllegalStateException(AppIllegalStateExceptionMessages.REVISION_NOT_PARENT);
        Map<UUID, ReceiptItem> originalItemsById = original.items.stream()
            .collect(Collectors.toMap(ReceiptItem::getId, Function.identity()));
        return items.stream()
            .filter(item -> item.getParentItem() != null)
            .flatMap(item -> Optional.ofNullable(originalItemsById.get(item.getParentItem().id()))
                .filter(originalItem -> originalItem.getNameDict() == null)
                .map(originalItem -> new ItemCorrection(originalItem.getId(), originalItem.getName(), item.getName(), item.getNameDict()))
                .stream())
            .toList();
    }

    void assignProductsByItemId(Map<UUID, ProductDictQuery> productsByItemId) {
        items.forEach(item -> Optional.ofNullable(productsByItemId.get(item.getId())).ifPresent(item::assignProduct));
    }

    void assignProductsByParentItemId(Map<UUID, ProductDictQuery> productsByParentItemId) {
        items.stream()
            .filter(item -> item.getParentItem() != null && item.getNameDict() == null)
            .forEach(item -> Optional.ofNullable(productsByParentItemId.get(item.getParentItem().id()))
                .ifPresent(item::assignProduct));
    }

    void renameItemsToProductNames(Function<ProductDictQuery, Optional<String>> productNames) {
        ensureCanEditItems();
        items.stream()
            .filter(item -> item.getNameDict() != null)
            .forEach(item -> productNames.apply(item.getNameDict()).ifPresent(item::rename));
    }

    private void markCorrectIfPricesConsistent() {
        if (isPriceConsistent()) this.isCorrect = true;
    }

    private boolean isPriceConsistent() {
        if (totalPrice == null || items.isEmpty()) return false;
        if (!items.stream().allMatch(ReceiptItem::isPriceConsistent)) return false;
        BigDecimal itemsSum = items.stream()
            .map(ReceiptItem::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return itemsSum.compareTo(totalPrice) == 0;
    }

    private void ensureCanEditItems() {
        if (!canEditItems()) throw new AppIllegalStateException(AppIllegalStateExceptionMessages.REVISION_NOT_USER_RESOLVER);
    }
}
