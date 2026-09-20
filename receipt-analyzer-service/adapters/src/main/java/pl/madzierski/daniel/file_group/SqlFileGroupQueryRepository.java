package pl.madzierski.daniel.file_group;

import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.madzierski.daniel.file_group.model.FileDto;
import pl.madzierski.daniel.file_group.model.FileGroupDto;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

interface SqlFileGroupQueryRepository extends JpaRepository<SqlFileGroup, String> {

    @Query(value = """
                SELECT rfg
                FROM SqlFileGroup rfg
                LEFT JOIN FETCH rfg.files rf
                WHERE rfg.receipt.id = :receiptId
        """)
    List<SqlFileGroup> findAllByReceiptId(@Param(value = "receiptId") String receiptId);
}

@AllArgsConstructor
@Repository
class FileGroupQueryRepositoryImpl implements FileGroupQueryRepository {

    private final SqlFileGroupQueryRepository sqlFileGroupQueryRepository;

    @Override
    public List<FileGroupDto> findAllByReceiptId(String receiptId) {
        return sqlFileGroupQueryRepository.findAllByReceiptId(receiptId)
            .stream()
            .map(it -> FileGroupDto.builder()
                .id(it.getId())
                .fileType(it.getFileType())
                .files(it.getFiles()
                    .stream()
                    .map(file -> FileDto.builder()
                        .id(file.getId())
                        .path(file.getPath())
                        .partNumber(file.getPartNumber())
                        .build())
                    .collect(Collectors.toSet()))
                .isOriginal(it.getIsOriginal())
                .build())
            .toList();
    }

    @Override
    public Optional<FileGroupDto> findFileGroupWithFiles(String fileGroupId) {
        return sqlFileGroupQueryRepository.findById(fileGroupId)
            .map(it -> FileGroupDto.builder()
                .id(it.getId())
                .fileType(it.getFileType())
                .files(it.getFiles()
                    .stream()
                    .map(file -> FileDto.builder()
                        .id(file.getId())
                        .path(file.getPath())
                        .partNumber(file.getPartNumber())
                        .build())
                    .collect(Collectors.toSet()))
                .isOriginal(it.getIsOriginal())
                .build());
    }
}
