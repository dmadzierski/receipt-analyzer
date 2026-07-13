package pl.madzierski.daniel.receipt;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pl.madzierski.daniel.exception.AppRuntimeException;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;
import pl.madzierski.daniel.file_group.FileFacade;
import pl.madzierski.daniel.file_group.FileQueryRepository;
import pl.madzierski.daniel.file_group.model.FileDto;
import pl.madzierski.daniel.file_group.model.FileGroupDto;
import pl.madzierski.daniel.product_dict.ProductDictFacade;
import pl.madzierski.daniel.product_dict.model.ProductAliasDto;
import pl.madzierski.daniel.product_dict.model.ProductDictDto;
import pl.madzierski.daniel.product_dict.model.ProductDictQueryEntity;
import pl.madzierski.daniel.receipt.model.*;
import pl.madzierski.daniel.receipt.scan_resolver.ReceiptResolverStrategyType;
import pl.madzierski.daniel.receipt.scan_resolver.service.ReceiptResolverLocatorService;
import pl.madzierski.daniel.wallet.model.WalletQueryEntity;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
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
    private final ProductDictFacade productDictFacade;

    ReceiptFacade(ReceiptItemRepository receiptItemRepository, ReceiptItemQueryRepository receiptItemQueryRepository, ReceiptItemFactory receiptItemFactory, ReceiptRevisionRepository revisionRepository, ReceiptRevisionFactory receiptRevisionFactory, ReceiptRepository receiptRepository, FileFacade fileFacade, ReceiptResolverLocatorService receiptResolverLocatorService, ReceiptQueryRepository receiptQueryRepository, ReceiptRevisionQueryRepository receiptRevisionQueryRepository, FileQueryRepository fileQueryRepository, @Lazy ProductDictFacade productDictFacade) {
        this.receiptItemRepository = receiptItemRepository;
        this.receiptItemQueryRepository = receiptItemQueryRepository;
        this.receiptItemFactory = receiptItemFactory;
        this.revisionRepository = revisionRepository;
        this.receiptRevisionFactory = receiptRevisionFactory;
        this.receiptRepository = receiptRepository;
        this.fileFacade = fileFacade;
        this.receiptResolverLocatorService = receiptResolverLocatorService;
        this.receiptQueryRepository = receiptQueryRepository;
        this.receiptRevisionQueryRepository = receiptRevisionQueryRepository;
        this.fileQueryRepository = fileQueryRepository;
        this.productDictFacade = productDictFacade;
    }

    @Transactional
    CreateReceiptResponse addReceipt(String userSub, MultipartFile file, CreateReceiptRequest body) {
        ReceiptEntity receipt = receiptRepository.save(createReceipt(body));
        FileGroupDto fileGroup = fileFacade.save(toDto(receipt), file, userSub);
        List<String> paths = fileGroup.files().stream().map(FileDto::getPath).toList();
        if (paths.isEmpty()) throw new AppRuntimeException(AppRuntimeExceptionMessages.FILE_NOT_FOUND);
        ReceiptRevisionResolveData revisionData = receiptResolverLocatorService.resolve(paths, body.strategy());
        ReceiptRevisionEntity revision = receiptRevisionFactory.from(revisionData);
        revision.setReceipt(receipt);
        receipt.addRevision(revision);
        revisionRepository.save(revision);
        return new CreateReceiptResponse(receipt.getId(), receipt.getName(), receipt.getDescription());
    }

    private ReceiptDto toDto(ReceiptEntity receipt) {
        return new ReceiptDto(receipt.getId(), receipt.getCreatedDate(), receipt.getModifiedDate(), receipt.getName(), receipt.getDescription(), receipt.getWallet());
    }

    private ReceiptEntity createReceipt(CreateReceiptRequest body) {
        String name = body.name() != null && !body.name().trim().isEmpty() ? body.name() : LocalDateTime.now(ZoneId.systemDefault()).toString();
        return ReceiptEntity.builder().name(name).description(body.description()).wallet(new WalletQueryEntity(body.walletId())).build();
    }

    @Transactional(readOnly = true)
    GetReceiptDetailsResponse getReceiptDetails(String receiptId) {
        ReceiptDto receiptDto = receiptQueryRepository.findReceiptEntityById(receiptId).orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.RECEIPT_NOT_FOUND));
        List<ReceiptRevisionDto> receiptRevisionList = receiptRevisionQueryRepository.getRevisionsByReceiptId(receiptId);
        String fileId = fileQueryRepository.findOriginalPdf(receiptId).map(FileDto::getId).orElse(null);
        Collection<ReceiptItemDto> itemListDto = Collections.emptyList();
        String preferredRevisionId = receiptRevisionList.stream().filter(ReceiptRevisionDto::getIsPreferredRevision).map(ReceiptRevisionDto::getId).findFirst().orElse(null);
        if (preferredRevisionId != null)
            itemListDto = receiptItemQueryRepository.findReceiptItemsByRevisionId(preferredRevisionId);
        return GetReceiptDetailsResponse.receiptDetailsMapper(receiptDto, receiptRevisionList.stream().filter(ReceiptRevisionDto::getIsPreferredRevision).findFirst().orElse(null), fileId, itemListDto, receiptRevisionList);
    }

    @Transactional(readOnly = true)
    public List<GetReceiptRevisionsResponse> getReceiptRevisions(String receiptId) {
        List<ReceiptRevisionDto> receiptRevisionEntitiesByReceiptId = receiptRevisionQueryRepository.getRevisionsByReceiptId(receiptId);
        return receiptRevisionEntitiesByReceiptId.stream().map(GetReceiptRevisionsResponse::receiptRevisionMapper).toList();
    }


    @Transactional
    RevisionCopyResponse createRevisionCopy(String revisionId) {
        ReceiptRevisionEntity revision = revisionRepository.findById(revisionId).orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.REVISION_NOT_FOUND));
        ReceiptRevisionEntity revisionCopy = ReceiptRevisionEntity.builder().name(revision.getName() + "(copy)").resolver(ReceiptResolverStrategyType.USER).brand(revision.getBrand()).totalPrice(revision.getTotalPrice()).payingDate(revision.getPayingDate()).address(revision.getAddress()).isPreferredRevision(false).isCorrect(revision.getIsCorrect()).receipt(revision.getReceipt()).parentReceiptRevision(revision).build();
        revisionRepository.save(revisionCopy);
        revision.getItems().forEach(item -> {
            ReceiptItemEntity newItem = new ReceiptItemEntity();
            newItem.setName(item.getName());
            newItem.setAmount(item.getAmount());
            newItem.setUnitPrice(item.getUnitPrice());
            newItem.setReceiptRevision(revisionCopy);
            newItem.setTotalPrice(item.getTotalPrice());
            newItem.setPosition(item.getPosition());
            newItem.setDiscount(item.getDiscount());
            newItem.setParentItem(new ReceiptItemEntity(item.getId()));
            if (item.getParentItem() != null) newItem.setNameDict(item.getNameDict());
            receiptItemRepository.save(newItem);
        });
        return new RevisionCopyResponse(revisionCopy.getId());
    }

    @Transactional(readOnly = true)
    GetRevisionResponse getRevision(String revisionId) {
        ReceiptRevisionDto revision = receiptRevisionQueryRepository.getRevisionById(revisionId).orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.REVISION_NOT_FOUND));
        Collection<ReceiptItemDto> items = receiptItemQueryRepository.findReceiptItemsByRevisionId(revisionId);
        return GetRevisionResponse.revisionMapper(revision, items);
    }

    @Transactional
    UpdateRevisionResponse updateRevision(String revisionId, UpdateRevisionRequest updatedRevisionRequest) {
        ReceiptRevisionEntity revision = revisionRepository.findById(revisionId).orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.REVISION_NOT_FOUND));
        if (updatedRevisionRequest.brand() != null) revision.setBrand(updatedRevisionRequest.brand());
        if (updatedRevisionRequest.totalPrice() != null) revision.setTotalPrice(updatedRevisionRequest.totalPrice());
        if (updatedRevisionRequest.payingDate() != null) revision.setPayingDate(updatedRevisionRequest.payingDate());
        if (updatedRevisionRequest.address() != null) revision.setAddress(updatedRevisionRequest.address());
        if (updatedRevisionRequest.isPreferredRevision() != null)
            revision.setIsPreferredRevision(updatedRevisionRequest.isPreferredRevision());
        if (updatedRevisionRequest.isCorrect() != null) revision.setIsCorrect(updatedRevisionRequest.isCorrect());
        revisionRepository.save(revision);
        if (updatedRevisionRequest.items() != null && revision.getResolver() == ReceiptResolverStrategyType.USER) {
            Set<String> incomingIds = updatedRevisionRequest.items().stream().map(UpdateRevisionRequest.ItemRequest::id).filter(Objects::nonNull).collect(Collectors.toSet());

            receiptItemRepository.deleteAllByIdInBatch(revision.getItems().stream().map(ReceiptItemEntity::getId).filter(id -> !incomingIds.contains(id)).toList());

            for (UpdateRevisionRequest.ItemRequest incomingItem : updatedRevisionRequest.items()) {
                if (incomingItem.id() != null && !incomingItem.id().trim().isEmpty()) {
                    revision.getItems().stream().filter(entity -> incomingItem.id().equals(entity.getId())).findFirst().ifPresent(entity -> {
                        entity.setName(incomingItem.name());
                        entity.setAmount(incomingItem.amount());
                        entity.setUnitPrice(incomingItem.unitPrice());
                        entity.setTotalPrice(incomingItem.totalPrice());
                        entity.setPosition(incomingItem.position());
                        if (incomingItem.discount() != null) entity.setDiscount(incomingItem.discount());
                        receiptItemRepository.save(entity);
                    });
                } else {
                    ProductDictQueryEntity productDict = productDictFacade.findCanonicalName(incomingItem.name()).map(item -> new ProductDictQueryEntity(item.getId())).orElse(null);
                    ReceiptItemEntity newItem = new ReceiptItemEntity(revision, incomingItem.name(), productDict, incomingItem.amount(), incomingItem.unitPrice(), 0.0, incomingItem.totalPrice(), incomingItem.position(), null);
                    receiptItemRepository.save(newItem);
                }
            }
        }
        return UpdateRevisionResponse.map(toDto(revision), revision.getItems().stream().map(this::toDto).collect(Collectors.toSet()));
    }
    public ReceiptItemDto toDto(ReceiptItemEntity item) {
        return ReceiptItemDto.builder().name(item.getName()).amount(item.getAmount()).unitPrice(item.getUnitPrice()).discount(item.getDiscount()).totalPrice(item.getTotalPrice()).position(item.getPosition()).build();
    }


    ReceiptRevisionDto toDto(ReceiptRevisionEntity revision) {
        return ReceiptRevisionDto.builder().id(revision.getId()).createdDate(revision.getCreatedDate()).name(revision.getName()).resolver(revision.getResolver()).brand(revision.getBrand()).totalPrice(revision.getTotalPrice()).payingDate(revision.getPayingDate()).address(revision.getAddress()).isPreferredRevision(revision.getIsPreferredRevision()).isCorrect(revision.getIsCorrect()).build();
    }


    @Transactional
    synchronized void updateDictByUserRevision(String revisionId) {
        Map<ProductDictDto, Collection<ReceiptItemDto>> receiptItemToProductDictNameMap = new HashMap<>();
        this.findAllMissingAliasesInRevision(revisionId).forEach(receiptItem -> {
            String alias = receiptItem.getParentItem().getName();
            String userText = receiptItem.getName();
            ProductDictDto resolvedDict;
            Optional<ProductDictDto> productDictEntityOptional = productDictFacade.findCanonicalName(alias);
            if (productDictEntityOptional.isPresent()) {
                resolvedDict = productDictEntityOptional.get();
                if (resolvedDict.getAliases().stream().noneMatch(currAlias -> currAlias.getName().equals(alias)))
                    resolvedDict.addAlias(ProductAliasDto.builder().name(alias).build());
                receiptItemToProductDictNameMap.computeIfAbsent(resolvedDict, k -> new HashSet<>()).add(receiptItem.getParentItem());
            } else {
                Optional<ProductDictDto> optionalProductDict = receiptItemToProductDictNameMap.keySet().stream().filter(dict -> dict.getAliases().stream().anyMatch(currAlias -> currAlias.getName().equals(userText))).findAny();
                if (optionalProductDict.isPresent()) {
                    resolvedDict = optionalProductDict.get();
                    resolvedDict.addAlias(ProductAliasDto.builder().name(alias).build());
                    receiptItemToProductDictNameMap.get(resolvedDict).add(receiptItem.getParentItem());
                } else {
                    resolvedDict = ProductDictDto.builder().name(userText).build();
                    resolvedDict.addAlias(ProductAliasDto.builder().name(userText).build());
                    if (!userText.equals(alias)) resolvedDict.addAlias(ProductAliasDto.builder().name(alias).build());
                    receiptItemToProductDictNameMap.put(resolvedDict, new HashSet<>(Set.of(receiptItem.getParentItem())));
                }
            }
        });
        productDictFacade.saveAll(receiptItemToProductDictNameMap.keySet());
        this.updateItemsProductDict(receiptItemToProductDictNameMap);
    }


    public void save(ReceiptRevisionDto receiptRevision) {
        ReceiptRevisionEntity receiptRevisionEntity = receiptRevisionFactory.from(receiptRevision);
        revisionRepository.save(receiptRevisionEntity);
        Collection<ReceiptItemDto> items = receiptRevision.getItems();
        receiptItemRepository.saveAll(items.stream().map(receiptItemDto -> {
            ReceiptItemEntity item = receiptItemFactory.from(receiptItemDto);
            item.setReceiptRevision(receiptRevisionEntity);
            return item;
        }).collect(Collectors.toSet()));
    }

    public List<ReceiptItemDto> findAllMissingAliasesInRevision(String revisionId) {
        return receiptItemQueryRepository.findAllMissingAliasesInRevision(revisionId);
    }

    @Transactional
    public void reassignProductDict(ProductDictQueryEntity primaryDict, List<String> productDictIdList) {
        receiptItemRepository.reassignProductDict(primaryDict, productDictIdList);
    }

    public void updateItemsProductDict(Map<ProductDictDto, Collection<ReceiptItemDto>> receiptItemToProductDictNameMap) {
        receiptItemRepository.saveAll(receiptItemToProductDictNameMap.entrySet().stream().flatMap(entry -> entry.getValue().stream().map(receiptItemDto -> {
            ReceiptItemEntity item = receiptItemFactory.from(receiptItemDto);
            item.setNameDict(new ProductDictQueryEntity(entry.getKey().getId()));
            return item;
        })).collect(Collectors.toSet()));
    }
}