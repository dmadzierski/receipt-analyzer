package pl.madzierski.daniel.app.receipt.revision;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.madzierski.daniel.app.product_dict.ProductDictEntity;
import pl.madzierski.daniel.app.product_dict.ProductDictProvider;
import pl.madzierski.daniel.app.product_dict.product_alias.ProductAliasEntity;
import pl.madzierski.daniel.app.receipt.ReceiptEntity;
import pl.madzierski.daniel.app.receipt.ReceiptProvider;
import pl.madzierski.daniel.app.receipt.revision.item.ReceiptItemEntity;
import pl.madzierski.daniel.app.receipt.revision.item.ReceiptItemProvider;
import pl.madzierski.daniel.app.receipt.revision.model.*;
import pl.madzierski.daniel.app.receipt.scan_resolver.ReceiptResolverStrategyType;
import pl.madzierski.daniel.exception.AppRuntimeException;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ReceiptRevisionService {

    private final ReceiptRevisionRepository revisionRepository;
    private final ReceiptProvider receiptProvider;
    private final ReceiptItemProvider receiptItemProvider;
    private final ProductDictProvider productDictProvider;

    private static ReceiptRevisionEntity getReceiptRevisionEntity(AddRevisionRequest revisionRequest, ReceiptEntity receiptEntity) {
        return new ReceiptRevisionEntity("", "1.0", ReceiptResolverStrategyType.USER, revisionRequest.brand(), revisionRequest.totalPrice(), revisionRequest.payingDate(), revisionRequest.address(), false, false, receiptEntity, null);
    }

    public AddRevisionResponse addRevision(AddRevisionRequest revisionRequest) {
        ReceiptEntity receiptEntity = receiptProvider.findById(revisionRequest.receiptId()).orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.RECEIPT_NOT_FOUND));

        ReceiptRevisionEntity receiptRevisionEntity = getReceiptRevisionEntity(revisionRequest, receiptEntity);

        ReceiptRevisionEntity revisionEntity = revisionRepository.save(receiptRevisionEntity);

        if (revisionRequest.items() != null) {
            Set<ReceiptItemEntity> mappedItems = revisionRequest.items().stream().map(item -> {
                ReceiptItemEntity parentItem = null;
                if (item.originalItemId() != null) {
                    parentItem = receiptItemProvider.findById(item.originalItemId()).orElse(null);
                }
                ProductDictEntity productDictEntity = productDictProvider.findCanonicalName(item.name()).orElse(null);
                return new ReceiptItemEntity(null, item.name(), productDictEntity, item.amount(), item.unitPrice(), item.discount(), item.totalPrice(), item.position(), parentItem, new HashSet<>());
            }).collect(Collectors.toSet());

            List<ReceiptItemEntity> savedItems = receiptItemProvider.saveAll(mappedItems);
            revisionEntity.getItems().addAll(savedItems);
        }

        return AddRevisionResponse.addRevisionMapper(receiptRevisionEntity);
    }

    @Transactional
    public RevisionCopyResponse createRevisionCopy(String revisionId) {
        ReceiptRevisionEntity revision = getRevisionById(revisionId);

        ReceiptRevisionEntity revisionCopy = new ReceiptRevisionEntity(revision.getName() + "(copy)", "", ReceiptResolverStrategyType.USER, revision.getBrand(), revision.getTotalPrice(), revision.getPayingDate(), revision.getAddress(), false, revision.getIsCorrect(), revision.getReceipt(), revision);

        Set<ReceiptItemEntity> copiedItems = revision.getItems().stream().map(item -> new ReceiptItemEntity(revisionCopy, item.getName(), item.getNameDict(), item.getAmount(), item.getUnitPrice(), item.getDiscount(), item.getTotalPrice(), item.getPosition(), item, new HashSet<>())
        ).collect(Collectors.toSet());
        revisionCopy.addItems(copiedItems);

        ReceiptRevisionEntity savedRevisionCopy = revisionRepository.save(revisionCopy);
        return new RevisionCopyResponse(savedRevisionCopy.getId());
    }

    public GetRevisionResponse getRevision(String revisionId) {
        return GetRevisionResponse.revisionMapper(revisionRepository.findById(revisionId).orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.REVISION_NOT_FOUND)));
    }

    @Transactional
    public UpdateRevisionResponse updateRevision(String revisionId, UpdateRevisionRequest updatedRevision) {
        ReceiptRevisionEntity currentRevision = getRevisionById(revisionId);

        if (updatedRevision.brand() != null) currentRevision.setBrand(updatedRevision.brand());
        if (updatedRevision.totalPrice() != null) currentRevision.setTotalPrice(updatedRevision.totalPrice());
        if (updatedRevision.payingDate() != null) currentRevision.setPayingDate(updatedRevision.payingDate());
        if (updatedRevision.address() != null) currentRevision.setAddress(updatedRevision.address());
        if (updatedRevision.isPreferredRevision() != null)
            currentRevision.setIsPreferredRevision(updatedRevision.isPreferredRevision());
        if (updatedRevision.isCorrect() != null) currentRevision.setIsCorrect(updatedRevision.isCorrect());

        if (updatedRevision.items() != null && currentRevision.getResolver() == ReceiptResolverStrategyType.USER) {
            Set<String> incomingIds = updatedRevision.items().stream()
                    .map(UpdateRevisionRequest.ItemRequest::id)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            currentRevision.getItems().stream()
                    .map(ReceiptItemEntity::getId)
                    .filter(id -> !incomingIds.contains(id))
                    .forEach(currentRevision::removeItem);

            for (UpdateRevisionRequest.ItemRequest incomingItem : updatedRevision.items()) {
                if (incomingItem.id() != null && !incomingItem.id().trim().isEmpty()) {
                    ReceiptItemEntity existingItem = currentRevision.getItems().stream().filter(it -> incomingItem.id().equals(it.getId())).findFirst().orElse(null);
                    if (existingItem != null) {
                        if (incomingItem.name() != null) existingItem.setName(incomingItem.name());
                        if (incomingItem.amount() != null) existingItem.setAmount(incomingItem.amount());
                        if (incomingItem.unitPrice() != null) existingItem.setUnitPrice(incomingItem.unitPrice());
                        if (incomingItem.totalPrice() != null) existingItem.setTotalPrice(incomingItem.totalPrice());
                        if (incomingItem.position() != null) existingItem.setPosition(incomingItem.position());
                    }
                } else {
                    ProductDictEntity productDictEntity = productDictProvider.findCanonicalName(incomingItem.name()).orElse(null);
                    ReceiptItemEntity newItem = new ReceiptItemEntity(currentRevision, incomingItem.name(), productDictEntity, incomingItem.amount(), incomingItem.unitPrice(), 0.0, incomingItem.totalPrice(), incomingItem.position(), null, new HashSet<>());
                    currentRevision.addItem(receiptItemProvider.save(newItem));
                }
            }
        }

        ReceiptRevisionEntity savedRevision = revisionRepository.save(currentRevision);
        return UpdateRevisionResponse.revisionMapper(savedRevision);
    }

    private ReceiptRevisionEntity getRevisionById(String revisionId) {
        return revisionRepository.findById(revisionId).orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.REVISION_NOT_FOUND));
    }

    @Transactional
    synchronized public void updateDictByUserRevision(String revisionId) {
        Map<ProductDictEntity, Collection<ReceiptItemEntity>> receiptItemToProductDictNameMap = new HashMap<>();
        receiptItemProvider.findAllMissingAliasesInRevision(revisionId).forEach(receiptItemEntity -> {
            String alias = receiptItemEntity.getParentItem().getName();
            String userText = receiptItemEntity.getName();
            ProductDictEntity resolvedDict;
            Optional<ProductDictEntity> productDictEntityOptional = productDictProvider.findCanonicalName(alias);
            if (productDictEntityOptional.isPresent()) {
                resolvedDict = productDictEntityOptional.get();
                if (resolvedDict.getAliases().stream().noneMatch(currAlias -> currAlias.getName().equals(alias)))
                    resolvedDict.addAlias(new ProductAliasEntity(alias));
                receiptItemToProductDictNameMap.computeIfAbsent(resolvedDict, k -> new HashSet<>()).add(receiptItemEntity.getParentItem());
            } else {
                Optional<ProductDictEntity> optionalProductDict = receiptItemToProductDictNameMap.keySet().stream().filter(dict -> dict.getAliases().stream().anyMatch(currAlias -> currAlias.getName().equals(userText))).findAny();
                if (optionalProductDict.isPresent()) {
                    resolvedDict = optionalProductDict.get();
                    resolvedDict.addAlias(new ProductAliasEntity(alias));
                    receiptItemToProductDictNameMap.get(resolvedDict).add(receiptItemEntity.getParentItem());
                } else {
                    resolvedDict = ProductDictEntity.builder().name(userText).build();
                    resolvedDict.addAlias(new ProductAliasEntity(userText));
                    if (!userText.equals(alias))
                        resolvedDict.addAlias(new ProductAliasEntity(alias));
                    receiptItemToProductDictNameMap.put(resolvedDict, new HashSet<>(Set.of(receiptItemEntity.getParentItem())));
                }
            }
        });
        productDictProvider.saveAll(receiptItemToProductDictNameMap.keySet());
        Set<ReceiptItemEntity> itemsToSave = receiptItemToProductDictNameMap.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream().peek(item -> item.setNameDict(entry.getKey())))
                .collect(Collectors.toSet());
        receiptItemProvider.saveAll(itemsToSave);
    }
}