package pl.madzierski.daniel.app.receipt;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pl.madzierski.daniel.app.file_group.FileGroupEntity;
import pl.madzierski.daniel.app.file_group.FileGroupProvider;
import pl.madzierski.daniel.app.file_group.file.FileEntity;
import pl.madzierski.daniel.app.product_dict.ProductDictEntity;
import pl.madzierski.daniel.app.product_dict.ProductDictProvider;
import pl.madzierski.daniel.app.receipt.model.CreateReceiptRequest;
import pl.madzierski.daniel.app.receipt.model.CreateReceiptResponse;
import pl.madzierski.daniel.app.receipt.model.GetReceiptDetailsResponse;
import pl.madzierski.daniel.app.receipt.model.GetReceiptRevisionsResponse;
import pl.madzierski.daniel.app.receipt.revision.ReceiptRevisionEntity;
import pl.madzierski.daniel.app.receipt.revision.RevisionProvider;
import pl.madzierski.daniel.app.receipt.revision.item.ReceiptItemEntity;
import pl.madzierski.daniel.app.receipt.revision.model.ReceiptRevisionResolveData;
import pl.madzierski.daniel.app.receipt.scan_resolver.service.ReceiptResolverLocatorService;
import pl.madzierski.daniel.app.wallet.WalletEntity;
import pl.madzierski.daniel.app.wallet.WalletProvider;
import pl.madzierski.daniel.exception.AppRuntimeException;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
class ReceiptService {

    private final ReceiptRepository receiptRepository;
    private final FileGroupProvider fileGroupProvider;
    private final ReceiptResolverLocatorService receiptResolverLocatorService;
    private final RevisionProvider revisionProvider;
    private final WalletProvider walletProvider;
    private final ProductDictProvider productDictProvider;

    @Transactional
    CreateReceiptResponse addReceipt(MultipartFile file, CreateReceiptRequest body) {
        ReceiptEntity receipt = createReceiptEntity(body);
        FileGroupEntity fileGroup = this.fileGroupProvider.saveReceiptFile(receipt, file);
        List<String> paths = fileGroup.getFiles().stream().map(FileEntity::getPath).toList();
        if (paths.isEmpty())
            throw new AppRuntimeException(AppRuntimeExceptionMessages.FILE_NOT_FOUND);
        ReceiptRevisionResolveData revisionData = receiptResolverLocatorService.resolve(paths, body.strategy());
        ReceiptRevisionEntity receiptRevision = mapToReceiptRevisionEntity(body, revisionData);
        receiptRevision.setReceipt(receipt);
        receipt.addRevision(receiptRevision);
        ReceiptEntity savedReceipt = this.saveReceipt(receipt);
        return new CreateReceiptResponse(savedReceipt.getId(), savedReceipt.getName(), savedReceipt.getDescription());
    }

    private ReceiptEntity createReceiptEntity(CreateReceiptRequest body) {
        String name = body.name() != null && !body.name().trim().isEmpty() ? body.name() : LocalDateTime.now().toString();
        ReceiptEntity receipt = ReceiptEntity.builder().name(name).description(body.description()).build();
        WalletEntity wallet = walletProvider.findWallet(body.walletId()).orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.WALLET_NOT_FOUND));
        receipt.setWallet(wallet);
        return this.saveReceipt(receipt);
    }

    private ReceiptRevisionEntity mapToReceiptRevisionEntity(
            CreateReceiptRequest body,
            ReceiptRevisionResolveData revisionData
    ) {
        double totalPrice = 0.0;
        if (revisionData.items() != null) {
            totalPrice = revisionData.items().stream()
                    .mapToDouble(it -> it.totalPrice() != null ? it.totalPrice() : 0.0)
                    .sum();
        }
        ReceiptRevisionEntity revisionEntity = new ReceiptRevisionEntity(body.name(), revisionData.revisionVersion(), body.strategy(), revisionData.brand(), totalPrice, LocalDateTime.now().toString(), null, false, false, null, null);


        if (revisionData.items() != null) {
            Set<ReceiptItemEntity> items = revisionData.items().stream().map(it -> {
                Optional<ProductDictEntity> canonicalName = productDictProvider.findCanonicalName(it.name());
                return new ReceiptItemEntity(revisionEntity, it.name(), canonicalName.orElse(null), it.amount(), it.unitPrice(), it.discount(), it.totalPrice(), it.position(), null);
            }).collect(Collectors.toSet());
            revisionEntity.addItems(items);
        }
        return revisionEntity;
    }

    private ReceiptEntity saveReceipt(ReceiptEntity receipt) {
        return receiptRepository.save(receipt);
    }

    @Transactional(readOnly = true)
    GetReceiptDetailsResponse getReceiptDetails(String receiptId) {
        ReceiptEntity receiptEntity = receiptRepository.findReceiptEntityWithItemAndProductDictById(receiptId);
        return GetReceiptDetailsResponse.receiptDetailsMapper(receiptEntity, receiptEntity.getReceiptRevisions().stream().filter(ReceiptRevisionEntity::getIsPreferredRevision).findFirst().orElse(null), Objects.requireNonNull(Objects.requireNonNull(receiptEntity.getFileGroups().stream().findFirst().orElse(null)).getFiles().stream().findFirst().orElse(null)).getId());
    }

    List<GetReceiptRevisionsResponse> getReceiptRevisions(String receiptId) {
        return revisionProvider.getReceiptRevisions(receiptId).stream()
                .map(GetReceiptRevisionsResponse::receiptRevisionMapper)
                .collect(Collectors.toList());
    }
}