package pl.madzierski.daniel.receipt;

import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import pl.madzierski.daniel.exception.AppRuntimeException;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;
import pl.madzierski.daniel.file_group.FileFacade;
import pl.madzierski.daniel.file_group.FileQueryRepository;
import pl.madzierski.daniel.file_group.model.FileDto;
import pl.madzierski.daniel.file_group.model.FileGroupDto;
import pl.madzierski.daniel.product_dict.ProductDictFacade;
import pl.madzierski.daniel.product_dict.model.ProductAliasDto;
import pl.madzierski.daniel.product_dict.model.ProductDictQuery;
import pl.madzierski.daniel.product_dict.model.ProductDto;
import pl.madzierski.daniel.receipt.model.*;
import pl.madzierski.daniel.receipt.scan_resolver.service.ReceiptResolverLocatorService;
import pl.madzierski.daniel.store.StoreQueryRepository;
import pl.madzierski.daniel.store.model.StoreDetailsResponse;
import pl.madzierski.daniel.store.model.StoreQuery;
import pl.madzierski.daniel.wallet.model.WalletQuery;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
public class ReceiptFacade {

    private final ReceiptItemRepository receiptItemRepository;
    private final ReceiptItemQueryRepository receiptItemQueryRepository;
    private final ReceiptItemFactory receiptItemFactory;
    private final ReceiptRevisionRepository revisionRepository;
    private final ReceiptRevisionFactory receiptRevisionFactory;
    private final ReceiptRepository receiptRepository;
    private final FileFacade fileFacade;
    private final ReceiptResolverLocatorService receiptResolverLocatorService;
    private final ReceiptQueryRepository receiptQueryRepository;
    private final ReceiptRevisionQueryRepository receiptRevisionQueryRepository;
    private final FileQueryRepository fileQueryRepository;
    private final StoreQueryRepository storeQueryRepository;
    private final ProductDictFacade productDictFacade;

    @Transactional
    CreateReceiptResponse addReceipt(String userSub, InputStream fileInputStream, String fileContentType, CreateReceiptRequest body) {
        Receipt receipt = receiptRepository.save(createReceipt(body));
        FileGroupDto fileGroup = fileFacade.save(toDto(receipt), fileInputStream, fileContentType, userSub);
        List<String> paths = fileGroup.files().stream().map(FileDto::getPath).toList();
        if (paths.isEmpty()) throw new AppRuntimeException(AppRuntimeExceptionMessages.FILE_NOT_FOUND);
        ReceiptRevisionResolveData revisionData = receiptResolverLocatorService.resolve(paths, body.strategy());
        ReceiptRevision revision = receiptRevisionFactory.from(revisionData);
        revision.setReceipt(receipt);
        revisionRepository.save(revision);
        return new CreateReceiptResponse(receipt.getId(), receipt.getName(), receipt.getDescription());
    }

    private ReceiptDto toDto(Receipt receipt) {
        return new ReceiptDto(receipt.getId(), receipt.getCreatedDate(), receipt.getModifiedDate(), receipt.getName(), receipt.getDescription(), receipt.getWallet());
    }

    private Receipt createReceipt(CreateReceiptRequest body) {
        String name = body.name() != null && !body.name().trim().isEmpty() ? body.name() : LocalDateTime.now(ZoneId.systemDefault()).toString();
        return Receipt.builder().name(name).description(body.description()).wallet(new WalletQuery(body.walletId())).store(new StoreQuery(body.storeId())).build();
    }

    @Transactional(readOnly = true)
    GetReceiptDetailsResponse getReceiptDetails(String receiptId) {
        ReceiptDto receiptDto = receiptQueryRepository.findById(receiptId).orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.RECEIPT_NOT_FOUND));
        List<ReceiptRevisionDto> receiptRevisionList = receiptRevisionQueryRepository.getRevisionsByReceiptId(receiptId);
        String fileId = fileQueryRepository.findOriginalPdf(receiptId).map(FileDto::getId).orElse(null);
        Collection<ReceiptItemDto> itemListDto = Collections.emptyList();
        String preferredRevisionId = receiptRevisionList.stream().filter(ReceiptRevisionDto::getIsPreferredRevision).map(ReceiptRevisionDto::getId).findFirst().orElse(null);
        if (preferredRevisionId != null)
            itemListDto = receiptItemQueryRepository.findReceiptItemsByRevisionId(preferredRevisionId);
        StoreDetailsResponse store = receiptDto.getStore() == null ? null : storeQueryRepository.findStoreById(receiptDto.getStore().getId())
            .map(StoreDetailsResponse::storeMapper)
            .orElse(null);
        return GetReceiptDetailsResponse.receiptDetailsMapper(receiptDto, receiptRevisionList.stream().filter(ReceiptRevisionDto::getIsPreferredRevision).findFirst().orElse(null), fileId, itemListDto, receiptRevisionList, store);
    }

    @Transactional(readOnly = true)
    List<GetReceiptRevisionsResponse> getReceiptRevisions(String receiptId) {
        List<ReceiptRevisionDto> receiptRevisionEntitiesByReceiptId = receiptRevisionQueryRepository.getRevisionsByReceiptId(receiptId);
        return receiptRevisionEntitiesByReceiptId.stream().map(GetReceiptRevisionsResponse::receiptRevisionMapper).toList();
    }


    @Transactional
    RevisionCopyResponse createRevisionCopy(String revisionId) {
        ReceiptRevision revision = revisionRepository.findByIdWithItems(revisionId).orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.REVISION_NOT_FOUND));
        ReceiptRevision revisionCopy = copyRevisionWithItems(revision);
        revisionRepository.save(revisionCopy);
        return new RevisionCopyResponse(revisionCopy.getId());
    }

    private static ReceiptRevision copyRevisionWithItems(ReceiptRevision revision) {
        ReceiptRevision revisionCopy = ReceiptRevision.builder().name(revision.getName() + "(copy)")
            .resolver(ReceiptResolverStrategyType.USER)
            .address(revision.getAddress()).isPreferredRevision(false)
            .totalPrice(revision.getTotalPrice())
            .paymentDate(revision.getPaymentDate())
            .isCorrect(revision.getIsCorrect())
            .receipt(revision.getReceipt())
            .parentReceiptRevision(revision)
            .build();
        revisionCopy.setItems(revision.getItems().stream().map(item -> {
            ReceiptItem newItem = new ReceiptItem();
            newItem.setName(item.getName());
            newItem.setAmount(item.getAmount());
            newItem.setUnitPrice(item.getUnitPrice());
            newItem.setReceiptRevision(revisionCopy);
            newItem.setTotalPrice(item.getTotalPrice());
            newItem.setPosition(item.getPosition());
            newItem.setDiscount(item.getDiscount());
            newItem.setParentItem(new ReceiptItem(item.getId()));
            if (item.getParentItem() != null) newItem.setNameDict(item.getNameDict());
            return newItem;
        }).collect(Collectors.toSet()));
        return revisionCopy;
    }

    @Transactional(readOnly = true)
    GetRevisionResponse getRevision(String revisionId) {
        ReceiptRevisionDto revision = receiptRevisionQueryRepository.getRevisionById(revisionId).orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.REVISION_NOT_FOUND));
        Collection<ReceiptItemDto> items = receiptItemQueryRepository.findReceiptItemsByRevisionId(revisionId);
        return GetRevisionResponse.revisionMapper(revision, items);
    }

    @Transactional
    UpdateRevisionResponse updateRevision(String revisionId, UpdateRevisionRequest updatedRevisionRequest) {
        ReceiptRevision revision = revisionRepository.findByIdWithItems(revisionId).orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.REVISION_NOT_FOUND));
        if (updatedRevisionRequest.brand() != null) revision.setBrand(updatedRevisionRequest.brand());
        if (updatedRevisionRequest.totalPrice() != null) revision.setTotalPrice(updatedRevisionRequest.totalPrice());
        if (updatedRevisionRequest.paymentDate() != null) revision.setPaymentDate(updatedRevisionRequest.paymentDate());
        if (updatedRevisionRequest.address() != null) revision.setAddress(updatedRevisionRequest.address());
        if (updatedRevisionRequest.isPreferredRevision() != null)
            revision.setIsPreferredRevision(updatedRevisionRequest.isPreferredRevision());
        if (updatedRevisionRequest.isCorrect() != null) revision.setIsCorrect(updatedRevisionRequest.isCorrect());
        if (updatedRevisionRequest.items() != null && revision.getResolver() == ReceiptResolverStrategyType.USER) {
            Set<String> incomingIds = updatedRevisionRequest.items().stream().map(UpdateRevisionRequest.ItemRequest::id).filter(Objects::nonNull).collect(Collectors.toSet());
            receiptItemRepository.deleteAllByIdIn(revision.getItems().stream().map(ReceiptItem::getId).filter(id -> !incomingIds.contains(id)).toList());
            for (UpdateRevisionRequest.ItemRequest incomingItem : updatedRevisionRequest.items()) {
                if (incomingItem.id() != null && !incomingItem.id().trim().isEmpty()) {
                    revision.getItems().stream().filter(entity -> incomingItem.id().equals(entity.getId())).findFirst().ifPresent(entity -> {
                        entity.setName(incomingItem.name());
                        entity.setAmount(incomingItem.amount());
                        entity.setUnitPrice(incomingItem.unitPrice());
                        entity.setDiscount(incomingItem.discount());
                        entity.setTotalPrice(incomingItem.totalPrice());
                        entity.setPosition(incomingItem.position());
                        entity.setParentItem(new ReceiptItem(entity.getParentItem().getId()));
                    });
                } else {
                    ProductDictQuery productDict = productDictFacade.findCanonicalName(incomingItem.name()).map(item -> new ProductDictQuery(item.getId())).orElse(null);
                    ReceiptItem newItem = new ReceiptItem(revision, incomingItem.name(), productDict, incomingItem.amount(), incomingItem.unitPrice(), incomingItem.discount(), incomingItem.totalPrice(), incomingItem.position(), null);
                    revision.addItem(newItem);
                }
            }
        }
        revisionRepository.save(revision);
        return UpdateRevisionResponse.map(toDto(revision), revision.getItems().stream().map(this::toDto).collect(Collectors.toSet()));
    }

    private ReceiptItemDto toDto(ReceiptItem item) {
        return ReceiptItemDto.builder().name(item.getName()).amount(item.getAmount()).unitPrice(item.getUnitPrice()).discount(item.getDiscount()).totalPrice(item.getTotalPrice()).position(item.getPosition()).build();
    }


    private ReceiptRevisionDto toDto(ReceiptRevision revision) {
        return ReceiptRevisionDto.builder().id(revision.getId()).createdDate(revision.getCreatedDate()).name(revision.getName()).resolver(revision.getResolver()).totalPrice(revision.getTotalPrice()).paymentDate(revision.getPaymentDate()).isPreferredRevision(revision.getIsPreferredRevision()).isCorrect(revision.getIsCorrect()).build();
    }

    @Transactional
    synchronized void updateDictByUserRevision(String revisionId) {
        Map<ProductDto, Collection<ReceiptItemDto>> receiptItemToProductDictNameMap = new HashMap<>();
        this.receiptItemQueryRepository.findAllMissingAliasesInRevision(revisionId).forEach(receiptItem -> {
            String alias = receiptItem.getParentItem().getName();
            String userText = receiptItem.getName();
            ProductDto resolvedDict;
            Optional<ProductDto> productDictEntityOptional = productDictFacade.findCanonicalName(alias);
            if (productDictEntityOptional.isPresent()) {
                resolvedDict = productDictEntityOptional.get();
                if (resolvedDict.getAliases().stream().noneMatch(currAlias -> currAlias.getName().equals(alias)))
                    resolvedDict.addAlias(ProductAliasDto.builder().name(alias).build());
                receiptItemToProductDictNameMap.computeIfAbsent(resolvedDict, k -> new HashSet<>()).add(receiptItem.getParentItem());
            } else {
                Optional<ProductDto> optionalProductDict = receiptItemToProductDictNameMap.keySet().stream().filter(dict -> dict.getAliases().stream().anyMatch(currAlias -> currAlias.getName().equals(userText))).findAny();
                if (optionalProductDict.isPresent()) {
                    resolvedDict = optionalProductDict.get();
                    resolvedDict.addAlias(ProductAliasDto.builder().name(alias).build());
                    receiptItemToProductDictNameMap.get(resolvedDict).add(receiptItem.getParentItem());
                } else {
                    resolvedDict = ProductDto.builder().name(userText).build();
                    resolvedDict.addAlias(ProductAliasDto.builder().name(userText).build());
                    if (!userText.equals(alias)) resolvedDict.addAlias(ProductAliasDto.builder().name(alias).build());
                    receiptItemToProductDictNameMap.put(resolvedDict, new HashSet<>(Set.of(receiptItem.getParentItem())));
                }
            }
        });
        productDictFacade.saveAll(receiptItemToProductDictNameMap.keySet());
        this.updateItemsProductDict(receiptItemToProductDictNameMap);
    }

    @Transactional
    public void reassignProductDict(ProductDictQuery primaryDict, List<String> productDictIdList) {
        receiptItemRepository.reassignProductDict(primaryDict, productDictIdList);
    }

    private void updateItemsProductDict(Map<ProductDto, Collection<ReceiptItemDto>> receiptItemToProductDictNameMap) {
        receiptItemRepository.saveAll(receiptItemToProductDictNameMap.entrySet().stream().flatMap(entry -> entry.getValue().stream().map(receiptItemDto -> {
            ReceiptItem item = receiptItemFactory.from(receiptItemDto);
            item.setNameDict(new ProductDictQuery(entry.getKey().getId()));
            return item;
        })).collect(Collectors.toSet()));
    }
}