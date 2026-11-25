package pl.madzierski.daniel.file_group;

import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import pl.madzierski.daniel.event.EventPublisher;
import pl.madzierski.daniel.file_group.event.FileGroupDeleted;
import pl.madzierski.daniel.user.model.UserQuery;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface SqlFileGroupRepository extends JpaRepository<SqlFileGroup, UUID> {
    @Query("SELECT g FROM SqlFileGroup g LEFT JOIN FETCH g.files WHERE g.id = :fileGroupId AND g.ownerId = :ownerId")
    Optional<SqlFileGroup> findById(UUID fileGroupId, UUID ownerId);

    @Query("SELECT g FROM SqlFileGroup g LEFT JOIN FETCH g.files WHERE g.id = :fileGroupId AND g.receiptId = :receiptId")
    Optional<SqlFileGroup> findByIdAndReceiptId(UUID fileGroupId, UUID receiptId);

    @Query("SELECT DISTINCT g FROM SqlFileGroup g LEFT JOIN FETCH g.files WHERE g.receiptId = :receiptId")
    List<SqlFileGroup> findAllByReceiptId(UUID receiptId);

    @Query("SELECT g.id FROM SqlFileGroup g WHERE g.receiptId = :receiptId")
    List<UUID> findIdsByReceiptId(UUID receiptId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM SqlFileGroup g WHERE g.receiptId = :receiptId")
    void deleteByReceiptId(UUID receiptId);
}

@AllArgsConstructor
@org.springframework.stereotype.Repository
class FileGroupRepositoryImpl implements FileGroupRepository {

    private final SqlFileGroupRepository fileGroupRepository;
    private final EventPublisher eventPublisher;

    @Override
    public FileGroup save(FileGroup fileGroup) {
        SqlFileGroup saved = this.fileGroupRepository.save(SqlFileGroup.fromFileGroup(fileGroup));
        eventPublisher.publishLatest(saved.getId(), () -> FileGroupEventMapper.changed(saved.toFileGroup()));
        return saved.toFileGroup();
    }

    @Override
    public Optional<FileGroup> findById(UUID fileGroupId, UserQuery user) {
        return this.fileGroupRepository.findById(fileGroupId, user.id()).map(SqlFileGroup::toFileGroup);
    }

    @Override
    public Optional<FileGroup> findByIdAndReceiptId(UUID fileGroupId, UUID receiptId) {
        return this.fileGroupRepository.findByIdAndReceiptId(fileGroupId, receiptId).map(SqlFileGroup::toFileGroup);
    }

    @Override
    public List<FileGroup> findAllByReceiptId(UUID receiptId) {
        return this.fileGroupRepository.findAllByReceiptId(receiptId).stream().map(SqlFileGroup::toFileGroup).toList();
    }

    @Override
    public void deleteById(UUID fileGroupId) {
        this.fileGroupRepository.deleteById(fileGroupId);
        eventPublisher.publish(new FileGroupDeleted(fileGroupId));
    }

    @Override
    public void deleteAllByReceiptId(UUID receiptId) {
        List<UUID> fileGroupIds = this.fileGroupRepository.findIdsByReceiptId(receiptId);
        this.fileGroupRepository.deleteByReceiptId(receiptId);
        fileGroupIds.forEach(fileGroupId -> eventPublisher.publish(new FileGroupDeleted(fileGroupId)));
    }
}
