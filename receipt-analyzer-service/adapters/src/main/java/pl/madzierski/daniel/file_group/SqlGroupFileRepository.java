package pl.madzierski.daniel.file_group;

import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import pl.madzierski.daniel.event.EventPublisher;
import pl.madzierski.daniel.file_group.event.FileGroupChanged;
import pl.madzierski.daniel.file_group.event.FileGroupDeleted;
import pl.madzierski.daniel.receipt.model.ReceiptQuery;
import pl.madzierski.daniel.user.model.UserQuery;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface SqlFileGroupRepository extends JpaRepository<SqlFileGroup, UUID> {
    SqlFileGroup save(SqlFileGroup sqlFileGroup);

    @Query("SELECT g FROM SqlFileGroup g LEFT JOIN FETCH g.files WHERE g.id = :fileGroupId AND g.ownerId = :ownerId")
    Optional<SqlFileGroup> findById(UUID fileGroupId, UUID ownerId);

    @Query("SELECT g FROM SqlFileGroup g LEFT JOIN FETCH g.files WHERE g.id = :fileGroupId AND g.receipt.id = :receiptId")
    Optional<SqlFileGroup> findByIdAndReceiptId(UUID fileGroupId, UUID receiptId);

    @Query("SELECT DISTINCT g FROM SqlFileGroup g LEFT JOIN FETCH g.files WHERE g.receipt.id = :receiptId")
    List<SqlFileGroup> findAllByReceiptId(UUID receiptId);

    @Query("SELECT COUNT(r) > 0 FROM SqlReceipt r WHERE r.id = :receiptId AND r.ownerId = :ownerId")
    boolean isReceiptAccessible(UUID receiptId, UUID ownerId);

    @Query("SELECT COUNT(rev) > 0 FROM SqlReceiptRevision rev WHERE rev.sourceFileGroup.id = :fileGroupId")
    boolean isUsedByRevision(UUID fileGroupId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM SqlFileGroup g WHERE g.receipt.id = :receiptId")
    void deleteByReceiptId(UUID receiptId);
}

@AllArgsConstructor
@org.springframework.stereotype.Repository
class FileGroupRepositoryImpl implements FileGroupRepository {

    private final SqlFileGroupRepository fileGroupRepository;
    private final EventPublisher eventPublisher;

    @Override
    public FileGroup save(FileGroup fileGroup) {
        SqlFileGroup entity = this.fileGroupRepository.save(SqlFileGroup.fromFileGroup(fileGroup));
        eventPublisher.publishLatest(entity.getId(), () -> {
            FileGroup saved = entity.toFileGroup();
            return new FileGroupChanged(saved.getId(), saved.getReceipt() != null ? saved.getReceipt().id() : null,
            saved.getOwner().id(), saved.getFileType(),
            saved.getFiles().stream().map(file -> new FileGroupChanged.File(file.getId(), file.getPath(), file.getPartNumber())).toList());
        });
        return entity.toFileGroup();
    }

    @Override
    public Optional<FileGroup> findById(UUID fileGroupId, UserQuery user) {
        return this.fileGroupRepository.findById(fileGroupId, user.id()).map(SqlFileGroup::toFileGroup);
    }

    @Override
    public Optional<FileGroup> findByIdAndReceipt(UUID fileGroupId, ReceiptQuery receipt) {
        return this.fileGroupRepository.findByIdAndReceiptId(fileGroupId, receipt.id()).map(SqlFileGroup::toFileGroup);
    }

    @Override
    public List<FileGroup> findAllByReceipt(ReceiptQuery receipt) {
        return this.fileGroupRepository.findAllByReceiptId(receipt.id()).stream().map(SqlFileGroup::toFileGroup).toList();
    }

    @Override
    public boolean isReceiptAccessible(ReceiptQuery receipt, UserQuery user) {
        return this.fileGroupRepository.isReceiptAccessible(receipt.id(), user.id());
    }

    @Override
    public boolean isUsedByRevision(UUID fileGroupId) {
        return this.fileGroupRepository.isUsedByRevision(fileGroupId);
    }

    @Override
    public void deleteById(UUID fileGroupId) {
        this.fileGroupRepository.deleteById(fileGroupId);
        eventPublisher.publish(new FileGroupDeleted(fileGroupId));
    }

    @Override
    public void deleteAllByReceiptId(UUID receiptId) {
        this.fileGroupRepository.deleteByReceiptId(receiptId);
    }
}
