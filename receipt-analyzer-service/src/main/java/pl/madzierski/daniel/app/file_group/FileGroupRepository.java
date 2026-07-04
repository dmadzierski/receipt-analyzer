package pl.madzierski.daniel.app.file_group;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
interface FileGroupRepository extends JpaRepository<FileGroupEntity, Long> {

    @Query(value = """
                SELECT
                    file.files_id
                FROM
                    receipt
                INNER JOIN receipt_file_group fileGroup ON
                    receipt.id = fileGroup.receipt_id
                INNER JOIN receipt_file_group_files file ON
                    fileGroup.id = file.file_group_entity_id
                WHERE
                    fileGroup.file_type = 'PDF'
                    AND fileGroup.is_original = true
                    AND receipt.id = :receiptId
                LIMIT 1
            """, nativeQuery = true)
    String findFirstOriginalPdf(String receiptId);
}