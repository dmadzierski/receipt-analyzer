package pl.madzierski.daniel.file_group;

import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.Repository;
import pl.madzierski.daniel.file_group.model.FileGroupDto;

import java.util.Optional;

interface SqlFileRepository extends JpaRepository<SqlFile, String> {
    Optional<SqlFile> findById(String fileId);
}
@AllArgsConstructor
@org.springframework.stereotype.Repository
class FileRepositoryImpl implements FileRepository {

    private final SqlFileRepository fileRepository;

    @Override
    public Optional<File> findById(String fileId) {
        return fileRepository.findById(fileId).map((SqlFile file) -> file.toFile(file.getFileGroup().toFileGroup()));
    }

    @Override
    public File save(File fileEntity) {
        return fileRepository.save(SqlFile.fromFile(fileEntity, SqlFileGroup.fromFileGroup(fileEntity.getFileGroup()))).toFile(fileEntity.getFileGroup());
    }

}
