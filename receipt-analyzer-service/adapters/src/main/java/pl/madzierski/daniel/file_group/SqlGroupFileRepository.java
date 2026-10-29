package pl.madzierski.daniel.file_group;

import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

interface SqlFileGroupRepository extends JpaRepository<SqlFileGroup, String> {
    SqlFileGroup save(SqlFileGroup sqlFileGroup);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
        DELETE FROM SqlFile f
        WHERE f.fileGroup.id IN (SELECT g.id FROM SqlFileGroup g WHERE g.receipt.id = :receiptId)
        """)
    void deleteFilesByReceiptId(String receiptId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM SqlFileGroup g WHERE g.receipt.id = :receiptId")
    void deleteByReceiptId(String receiptId);
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
    public void deleteById(String fileGroupId) {
        this.fileGroupRepository.deleteById(fileGroupId);
    }

    @Override
    public void deleteAllByReceiptId(String receiptId) {
        this.fileGroupRepository.deleteFilesByReceiptId(receiptId);
        this.fileGroupRepository.deleteByReceiptId(receiptId);
    }
}
