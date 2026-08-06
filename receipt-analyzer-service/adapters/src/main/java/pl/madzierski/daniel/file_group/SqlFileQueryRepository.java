package pl.madzierski.daniel.file_group;

import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import pl.madzierski.daniel.file_group.model.FileDto;

import java.util.Optional;

public interface SqlFileQueryRepository extends Repository<SqlFile, String> {

    @Query(nativeQuery = true, value = """
                SELECT rf.id, rf.path, rf.part_number
                FROM receipt_file rf
                INNER JOIN receipt_file_group rfg ON rfg.id = rf.file_group_id
                WHERE rfg.receipt_id = :receiptId AND rfg.file_type = 'PDF' AND rfg.is_original = true
        """)
    Optional<FileDto> findOriginalPdf(String receiptId);
}

@AllArgsConstructor
@org.springframework.stereotype.Repository
class FileQueryRepositoryImpl implements FileQueryRepository {

    private final SqlFileQueryRepository sqlFileQueryRepository;

    @Override
    public Optional<FileDto> findOriginalPdf(String receiptId) {
        return sqlFileQueryRepository.findOriginalPdf(receiptId);
    }
}
