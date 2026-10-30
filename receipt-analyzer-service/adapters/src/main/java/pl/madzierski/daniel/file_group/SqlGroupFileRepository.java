package pl.madzierski.daniel.file_group;

import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

interface SqlFileGroupRepository extends JpaRepository<SqlFileGroup, UUID> {
    SqlFileGroup save(SqlFileGroup sqlFileGroup);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
        DELETE FROM SqlFile f
        WHERE f.fileGroup.id IN (SELECT g.id FROM SqlFileGroup g WHERE g.receipt.id = :receiptId)
        """)
    void deleteFilesByReceiptId(UUID receiptId);

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
    public void flush() {
        this.fileGroupRepository.flush();
    }

    @Override
    public void deleteById(UUID fileGroupId) {
        this.fileGroupRepository.deleteById(fileGroupId);
    }

    @Override
    public void deleteAllByReceiptId(UUID receiptId) {
        this.fileGroupRepository.deleteFilesByReceiptId(receiptId);
        this.fileGroupRepository.deleteByReceiptId(receiptId);
    }
}
