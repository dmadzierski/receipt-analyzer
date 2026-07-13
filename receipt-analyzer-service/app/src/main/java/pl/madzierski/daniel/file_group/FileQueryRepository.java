package pl.madzierski.daniel.file_group;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.madzierski.daniel.file_group.model.FileDto;

import java.util.Optional;

public interface FileQueryRepository extends JpaRepository<FileEntity, String> {

    @Query(nativeQuery = true, value = """
                SELECT rf.id, rf.path, rf.part_number
                FROM receipt_file rf
                INNER JOIN receipt_file_group rfg ON rfg.id = rf.file_group_id
                WHERE rfg.receipt_id = :receiptId AND rfg.file_type = 'PDF' AND rfg.is_original = true
        """)
    Optional<FileDto> findOriginalPdf(String receiptId);
}
