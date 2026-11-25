package pl.madzierski.daniel.receipt;

import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import pl.madzierski.daniel.exception.AppRuntimeException;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;
import pl.madzierski.daniel.file_group.FileFacade;
import pl.madzierski.daniel.file_group.FileGroupQueryRepository;
import pl.madzierski.daniel.file_group.model.FileGroupQuery;
import pl.madzierski.daniel.product_dict.ProductDictFacade;
import pl.madzierski.daniel.product_dict.model.AliasToLearn;
import pl.madzierski.daniel.product_dict.model.ProductDictQuery;
import pl.madzierski.daniel.receipt.model.*;
import pl.madzierski.daniel.digitization.DigitizationFacade;
import pl.madzierski.daniel.digitization.model.DigitizedReceipt;
import pl.madzierski.daniel.store.StoreFacade;
import pl.madzierski.daniel.store.StoreQueryRepository;
import pl.madzierski.daniel.store.model.StoreDto;
import pl.madzierski.daniel.store.model.StoreQuery;
import pl.madzierski.daniel.user.model.AuthenticatedUser;
import pl.madzierski.daniel.wallet.WalletFacade;
import pl.madzierski.daniel.wallet.WalletQueryRepository;

import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
public class ReceiptFacade {

    private final ReceiptItemQueryRepository receiptItemQueryRepository;
    private final ReceiptRevisionRepository revisionRepository;
    private final ReceiptFactory receiptFactory;
    private final ReceiptRevisionFactory receiptRevisionFactory;
    private final ReceiptRepository receiptRepository;
    private final FileFacade fileFacade;
    private final DigitizationFacade digitizationFacade;
    private final ReceiptQueryRepository receiptQueryRepository;
    private final ReceiptRevisionQueryRepository receiptRevisionQueryRepository;
    private final StoreQueryRepository storeQueryRepository;
    private final ProductDictFacade productDictFacade;
    private final FileGroupQueryRepository fileGroupQueryRepository;
    private final WalletQueryRepository walletQueryRepository;
    private final WalletFacade walletFacade;
    private final StoreFacade storeFacade;

    @Transactional
    CreateReceiptResponse addReceipt(AuthenticatedUser user, List<byte[]> files, CreateReceiptRequest body) {
        if (!walletFacade.isWalletAccessible(user, body.walletId()))
            throw new AppRuntimeException(AppRuntimeExceptionMessages.WALLET_NOT_FOUND);
        DigitizedReceipt digitizedReceipt = digitizationFacade.digitize(files, body.strategy());
        Receipt receipt = receiptRepository.save(receiptFactory.from(body, user));
        UUID fileGroupId = fileFacade.save(user, receipt.getId(), files);
        ReceiptRevision revision = receiptRevisionFactory.from(digitizedReceipt, new ReceiptQuery(receipt.getId()), new FileGroupQuery(fileGroupId));
        revision.assignProducts(productDictFacade::findCanonicalNames);
        revisionRepository.save(revision);
        return new CreateReceiptResponse(receipt.getId());
    }

    @Transactional(readOnly = true)
    GetReceiptDetailsResponse getReceiptDetails(AuthenticatedUser user, UUID receiptId) {
        ReceiptDto receipt = receiptQueryRepository.findById(receiptId, user.id())
            .orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.RECEIPT_NOT_FOUND));
        List<ReceiptRevisionDto> revisions = receiptRevisionQueryRepository.getRevisionsByReceiptId(receiptId, user.id());
        ReceiptRevisionDto preferredRevision = revisions.stream()
            .filter(revision -> Boolean.TRUE.equals(revision.getIsPreferredRevision()))
            .findFirst()
            .orElse(null);
        Collection<ReceiptItemDto> items = preferredRevision == null ? List.of()
            : receiptItemQueryRepository.findReceiptItemsByRevisionId(preferredRevision.getId());
        StoreDto store = Optional.ofNullable(receipt.storeId())
            .flatMap(storeQueryRepository::findStoreById)
            .orElse(null);
        return new GetReceiptDetailsResponse(receipt, preferredRevision,
            fileGroupQueryRepository.findAllByReceiptId(receiptId), items, revisions, store);
    }

    GetWalletReceiptsResponse getWalletReceipts(AuthenticatedUser user, UUID walletId) {
        walletQueryRepository.getWalletDetails(walletId, user.id())
            .orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.WALLET_NOT_FOUND));
        Collection<WalletReceiptDto> receipts = receiptQueryRepository.getWalletReceipts(walletId);
        Set<UUID> storeIds = receipts.stream()
            .map(WalletReceiptDto::storeId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        Map<UUID, StoreDto> stores = storeQueryRepository.findStoresByIds(storeIds).stream()
            .collect(Collectors.toMap(StoreDto::getId, store -> store));
        return new GetWalletReceiptsResponse(receipts, stores);
    }

    @Transactional(readOnly = true)
    List<GetReceiptRevisionsResponse> getReceiptRevisions(AuthenticatedUser user, UUID receiptId) {
        List<ReceiptRevisionDto> receiptRevisionEntitiesByReceiptId = receiptRevisionQueryRepository.getRevisionsByReceiptId(receiptId, user.id());
        return receiptRevisionEntitiesByReceiptId.stream()
            .map(GetReceiptRevisionsResponse::receiptRevisionMapper)
            .toList();
    }

    @Transactional
    RevisionCopyResponse createRevisionCopy(AuthenticatedUser user, UUID revisionId) {
        ReceiptRevision revision = revisionRepository.findByIdWithItems(revisionId, user.toUserQuery())
            .orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.REVISION_NOT_FOUND));
        return new RevisionCopyResponse(revisionRepository.save(revision.copyAsUserRevision()).getId());
    }

    @Transactional(readOnly = true)
    GetRevisionResponse getRevision(AuthenticatedUser user, UUID revisionId) {
        ReceiptRevisionDto revision = receiptRevisionQueryRepository.getRevisionById(revisionId, user.id())
            .orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.REVISION_NOT_FOUND));
        Collection<ReceiptItemDto> items = receiptItemQueryRepository.findReceiptItemsByRevisionId(revisionId);
        return new GetRevisionResponse(revision, items);
    }

    @Transactional
    UpdateRevisionResponse updateRevision(AuthenticatedUser user, UUID revisionId, UpdateRevisionRequest updatedRevisionRequest) {
        ReceiptRevision revision = revisionRepository.findByIdWithItems(revisionId, user.toUserQuery())
            .orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.REVISION_NOT_FOUND));
        ensureVersion(revision.getVersion(), updatedRevisionRequest.version());
        if (revision.canEditItems()) {
            List<ReceiptItemChange> itemChanges = updatedRevisionRequest.items().stream()
                .map(ReceiptMapper::toItemChange)
                .toList();
            validateItemChanges(itemChanges);
            revision.updateItems(itemChanges);
            revision.assignProductsToNewItems(productDictFacade::findCanonicalNames);
        }
        revision.changeSummary(updatedRevisionRequest.totalPrice(), updatedRevisionRequest.paymentDate());
        revision.markCorrect(Boolean.TRUE.equals(updatedRevisionRequest.isCorrect()));
        if (updatedRevisionRequest.isPreferredRevision() != null)
            changePreferredRevision(user, revision, updatedRevisionRequest.isPreferredRevision());
        return new UpdateRevisionResponse(revisionRepository.save(revision).getId());
    }

    private void changePreferredRevision(AuthenticatedUser user, ReceiptRevision revision, boolean preferred) {
        Receipt receipt = receiptRepository.findAccessible(revision.getReceipt().id(), user.toUserQuery())
            .orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.RECEIPT_NOT_FOUND));
        if (preferred) receipt.choosePreferredRevision(revision);
        else receipt.unmarkPreferredRevision(revision);
        receiptRepository.save(receipt);
    }

    @Transactional
    void learnProductsFromCorrections(AuthenticatedUser user, UUID revisionId) {
        ReceiptRevision userRevision = revisionRepository.findByIdWithItems(revisionId, user.toUserQuery())
            .orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.REVISION_NOT_FOUND));
        if (!userRevision.canEditItems()) throw new AppRuntimeException(AppRuntimeExceptionMessages.REVISION_NOT_USER_RESOLVER);
        if (userRevision.getParentReceiptRevision() == null) throw new AppRuntimeException(AppRuntimeExceptionMessages.REVISION_WITHOUT_PARENT);
        ReceiptRevision originalRevision = revisionRepository.findByIdWithItems(userRevision.getParentReceiptRevision().id(), user.toUserQuery())
            .orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.REVISION_NOT_FOUND));
        List<ItemCorrection> corrections = userRevision.itemCorrections(originalRevision);
        if (corrections.isEmpty()) return;
        Map<UUID, AliasToLearn> aliasesByOriginalItemId = corrections.stream()
            .collect(Collectors.toMap(ItemCorrection::originalItemId,
                item -> new AliasToLearn(item.originalName(), item.correctedName(),
                    item.chosenProduct() != null ? item.chosenProduct().id() : null), (first, second) -> first));
        Map<AliasToLearn, ProductDictQuery> learnedProducts = productDictFacade.learnAliases(aliasesByOriginalItemId.values());
        Map<UUID, ProductDictQuery> productsByOriginalItemId = aliasesByOriginalItemId.entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> learnedProducts.get(entry.getValue())));
        originalRevision.assignProductsByItemId(productsByOriginalItemId);
        userRevision.assignProductsByParentItemId(productsByOriginalItemId);
        revisionRepository.save(originalRevision);
        revisionRepository.save(userRevision);
    }

    @Transactional
    void updateProductDictInRevisionItems(AuthenticatedUser user, UUID revisionId) {
        ReceiptRevision revision = revisionRepository.findByIdWithItems(revisionId, user.toUserQuery())
            .orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.REVISION_NOT_FOUND));
        revision.assignProducts(productDictFacade::findCanonicalNames);
        revisionRepository.save(revision);
    }

    private static void ensureVersion(Long current, Long expected) {
        if (!Objects.equals(current, expected)) throw new AppRuntimeException(AppRuntimeExceptionMessages.VERSION_CONFLICT);
    }

    private void validateItemChanges(Collection<ReceiptItemChange> itemChanges) {
        Set<UUID> productIds = itemChanges.stream()
            .map(ReceiptItemChange::nameDict)
            .filter(Objects::nonNull)
            .map(ProductDictQuery::id)
            .collect(Collectors.toSet());
        if (!productDictFacade.allProductsExist(productIds))
            throw new AppRuntimeException(AppRuntimeExceptionMessages.PRODUCT_NOT_FOUND);
    }

    @Transactional
    CreateRevisionResponse createRevision(AuthenticatedUser user, UUID receiptId, CreateRevisionRequest body) {
        Receipt receipt = receiptRepository.findAccessible(receiptId, user.toUserQuery())
            .orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.RECEIPT_NOT_FOUND));
        List<byte[]> files = fileFacade.getFiles(receiptId, body.fileGroupId());
        if (files.isEmpty()) throw new AppRuntimeException(AppRuntimeExceptionMessages.FILE_NOT_FOUND);
        DigitizedReceipt digitizedReceipt = digitizationFacade.digitize(files, body.strategy());
        ReceiptRevision revision = receiptRevisionFactory.from(digitizedReceipt, new ReceiptQuery(receipt.getId()), new FileGroupQuery(body.fileGroupId()));
        revision.assignProducts(productDictFacade::findCanonicalNames);
        return new CreateRevisionResponse(revisionRepository.save(revision).getId());
    }

    @Transactional
    UpdateReceiptResponse updateReceipt(AuthenticatedUser user, UUID receiptId, UpdateReceiptRequest request) {
        Receipt receipt = receiptRepository.findAccessible(receiptId, user.toUserQuery())
            .orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.RECEIPT_NOT_FOUND));
        ensureVersion(receipt.getVersion(), request.version());
        if (!storeFacade.storeExists(request.storeId()))
            throw new AppRuntimeException(AppRuntimeExceptionMessages.STORE_NOT_FOUND);
        receipt.updateData(request.name().trim(), request.description(), new StoreQuery(request.storeId()));
        Receipt savedReceipt = receiptRepository.save(receipt);
        return new UpdateReceiptResponse(savedReceipt.getId());
    }

    @Transactional
    void deleteReceipt(AuthenticatedUser user, UUID receiptId) {
        receiptRepository.findAccessible(receiptId, user.toUserQuery())
            .orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.RECEIPT_NOT_FOUND));
        revisionRepository.deleteAllByReceiptId(receiptId);
        fileFacade.deleteFileGroupsByReceiptId(receiptId);
        receiptRepository.deleteById(receiptId);
    }

    @Transactional
    void deleteRevision(AuthenticatedUser user, UUID revisionId) {
        ReceiptRevision revision = revisionRepository.findAccessible(revisionId, user.toUserQuery())
            .orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.REVISION_NOT_FOUND));
        if (revisionRepository.hasChildRevisions(revisionId))
            throw new AppRuntimeException(AppRuntimeExceptionMessages.CAN_NOT_DELETE_REVISION_WITH_CHILD_REVISIONS);
        Receipt receipt = receiptRepository.findAccessible(revision.getReceipt().id(), user.toUserQuery())
            .orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.RECEIPT_NOT_FOUND));
        if (receipt.isPreferred(revision)) {
            receipt.unmarkPreferredRevision(revision);
            receiptRepository.save(receipt);
        }
        revisionRepository.deleteById(revisionId);
    }

    @Transactional
    void rewriteProductNameToItems(AuthenticatedUser user, UUID revisionId) {
        ReceiptRevision revision = revisionRepository.findAccessible(revisionId, user.toUserQuery())
            .orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.REVISION_NOT_FOUND));
        if (!revision.canEditItems()) throw new AppRuntimeException(AppRuntimeExceptionMessages.REVISION_NOT_USER_RESOLVER);
        revision.renameItemsToProductNames(nameDict -> productDictFacade.findProductName(nameDict.id()));
        revisionRepository.save(revision);
    }
}