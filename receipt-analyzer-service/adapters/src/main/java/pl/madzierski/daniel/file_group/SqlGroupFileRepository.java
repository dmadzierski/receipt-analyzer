package pl.madzierski.daniel.file_group;

import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import pl.madzierski.daniel.user.model.UserQuery;

import java.util.Optional;
import java.util.UUID;

interface SqlFileGroupRepository extends JpaRepository<SqlFileGroup, UUID> {
    SqlFileGroup save(SqlFileGroup sqlFileGroup);

    @Query("SELECT g FROM SqlFileGroup g LEFT JOIN FETCH g.files WHERE g.id = :fileGroupId AND g.ownerId = :ownerId")
    Optional<SqlFileGroup> findById(UUID fileGroupId, UUID ownerId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM SqlFileGroup g WHERE g.receipt.id = :receiptId")
    void deleteByReceiptId(UUID receiptId);
}
@AllArgsConstructor
@org.springframework.stereotype.Repository
class FileGroupRepositoryImpl implements FileGroupRepository {

    private final SqlFileGroupRepository fileGroupRepository;

    @Override
    public FileGroup save(FileGroup fileGroup) {
        return this.fileGroupRepository.save(SqlFileGroup.fromFileGroup(fileGroup)).toFileGroup();
    }

    @Override
    public Optional<FileGroup> findById(UUID fileGroupId, UserQuery user) {
        return this.fileGroupRepository.findById(fileGroupId, user.id()).map(SqlFileGroup::toFileGroup);
    }

    @Override
    public void deleteById(UUID fileGroupId) {
        this.fileGroupRepository.deleteById(fileGroupId);
    }

    @Override
    public void deleteAllByReceiptId(UUID receiptId) {
        this.fileGroupRepository.deleteByReceiptId(receiptId);
    }
}
