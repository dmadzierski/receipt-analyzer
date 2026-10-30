package pl.madzierski.daniel.file_group;

import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface SqlFileRepository extends JpaRepository<SqlFile, UUID> {
    Optional<SqlFile> findById(UUID fileId);
}
@AllArgsConstructor
@org.springframework.stereotype.Repository
class FileRepositoryImpl implements FileRepository {

    private final SqlFileRepository fileRepository;

    @Override
    public Optional<File> findById(UUID fileId) {
        return fileRepository.findById(fileId).map((SqlFile file) -> file.toFile(file.getFileGroup().toFileGroup()));
    }

    @Override
    public File save(File fileEntity) {
        return fileRepository.save(SqlFile.fromFile(fileEntity, SqlFileGroup.fromFileGroup(fileEntity.getFileGroup()))).toFile(fileEntity.getFileGroup());
    }

}
