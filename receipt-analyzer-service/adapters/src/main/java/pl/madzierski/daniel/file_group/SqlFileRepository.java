package pl.madzierski.daniel.file_group;

import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.madzierski.daniel.user.model.UserQuery;

import java.util.Optional;
import java.util.UUID;

interface SqlFileRepository extends JpaRepository<SqlFile, UUID> {
    @Query("SELECT f FROM SqlFile f JOIN FETCH f.fileGroup g WHERE f.id = :fileId AND g.ownerId = :ownerId")
    Optional<SqlFile> findAccessible(UUID fileId, UUID ownerId);
}
@AllArgsConstructor
@org.springframework.stereotype.Repository
class FileRepositoryImpl implements FileRepository {

    private final SqlFileRepository fileRepository;

    @Override
    public Optional<File> findAccessible(UUID fileId, UserQuery user) {
        return fileRepository.findAccessible(fileId, user.getId()).map((SqlFile file) -> file.toFile(file.getFileGroup().toFileGroup()));
    }

    @Override
    public File save(File fileEntity) {
        return fileRepository.save(SqlFile.fromFile(fileEntity, SqlFileGroup.fromFileGroup(fileEntity.getFileGroup()))).toFile(fileEntity.getFileGroup());
    }

}
