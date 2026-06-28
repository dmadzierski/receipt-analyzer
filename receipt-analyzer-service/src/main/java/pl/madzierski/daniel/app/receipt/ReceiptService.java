package pl.madzierski.daniel.app.receipt;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pl.madzierski.daniel.app.file_group.FileGroupEntity;
import pl.madzierski.daniel.app.file_group.FileGroupProvider;
import pl.madzierski.daniel.app.file_group.file.FileEntity;
import pl.madzierski.daniel.app.receipt.model.*;
import pl.madzierski.daniel.app.receipt.revision.ReceiptRevisionEntity;
import pl.madzierski.daniel.app.receipt.revision.ReceiptRevisionRepository;
import pl.madzierski.daniel.app.receipt.revision.RevisionProvider;
import pl.madzierski.daniel.app.receipt.revision.item.ItemEntity;
import pl.madzierski.daniel.app.receipt.revision.model.ReceiptRevisionResolveData;
import pl.madzierski.daniel.app.receipt.scan_resolver.service.ReceiptResolverLocatorService;
import pl.madzierski.daniel.exception.AppRuntimeException;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;
import pl.madzierski.daniel.security.SecurityUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ReceiptService {

    private final ReceiptRepository receiptRepository;
    private final ReceiptRevisionRepository receiptRevisionRepository;
    private final FileGroupProvider fileGroupProvider;
    private final ReceiptResolverLocatorService receiptResolverLocatorService;
    private final RevisionProvider revisionProvider;

    @Transactional
    public CreateReceiptResponse addReceipt(MultipartFile file, CreateReceiptRequest body) {
        ReceiptEntity receipt = this.saveReceipt(createReceipt(body));
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

    private ReceiptEntity createReceipt(CreateReceiptRequest body) {
        String name = (body != null && body.name() != null && !body.name().trim().isEmpty())
                ? body.name()
                : LocalDateTime.now().toString();
        String description = (body != null) ? body.description() : null;
        return ReceiptEntity.builder().name(name).description(description).userSub(SecurityUtils.getCurrentUserSub()).build();
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
            Set<ItemEntity> items = revisionData.items().stream().map(it ->
                    new ItemEntity(revisionEntity, it.name(), it.amount(), it.unitPrice(), it.discount(), it.totalPrice(), it.position(), null, null)).collect(Collectors.toSet());
            revisionEntity.addItems(items);
        }
        return revisionEntity;
    }

    private ReceiptEntity saveReceipt(ReceiptEntity receipt) {
        return receiptRepository.save(receipt);
    }

    public GetReceiptListResponse getReceiptList() {
        return new GetReceiptListResponse(
                receiptRepository.getReceiptList(SecurityUtils.getCurrentUserSub()).stream().map(receiptEntity -> new GetReceiptListResponse.GetReceiptListItemResponse(
                        receiptEntity.getId(),
                        receiptEntity.getName(),
                        receiptEntity.getDescription(),
                        receiptEntity.getCreatedDate()
                )).toList()
        );
    }

    @Transactional(readOnly = true)
    public GetReceiptDetailsResponse getReceiptDetails(String receiptId) {
        ReceiptEntity receiptEntity = receiptRepository.findReceiptEntityById(receiptId);
        String preferredRevisionId = receiptEntity.getReceiptRevisions().stream()
                .filter(it -> Boolean.TRUE.equals(it.getIsPreferredRevision()))
                .map(ReceiptRevisionEntity::getId)
                .findFirst()
                .orElse(null);
        ReceiptRevisionEntity fullRevisionEntity = null;
        if (preferredRevisionId != null)
            fullRevisionEntity = receiptRevisionRepository.findReceiptRevisionEntitiesById(preferredRevisionId);
        String fileId = fileGroupProvider.getOriginalPdf(receiptEntity.getId());
        return GetReceiptDetailsResponse.receiptDetailsMapper(receiptEntity, fullRevisionEntity, fileId);
    }

    public List<GetReceiptRevisionsResponse> getReceiptRevisions(String receiptId) {
        return revisionProvider.getReceiptRevisions(receiptId).stream()
                .map(GetReceiptRevisionsResponse::receiptRevisionMapper)
                .collect(Collectors.toList());
    }
}