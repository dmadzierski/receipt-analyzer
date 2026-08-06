package pl.madzierski.daniel.file_group;

import lombok.AllArgsConstructor;
import org.springframework.data.repository.Repository;

import java.util.Optional;

interface SqlFileRepository extends Repository<SqlFile, String> {
    Optional<SqlFile> findById(String fileId);
}

@AllArgsConstructor
@org.springframework.stereotype.Repository
class FileRepositoryImpl implements FileRepository {

    private final SqlFileRepository sqlFileRepository;

    @Override
    public Optional<File> findById(String fileId) {
        return sqlFileRepository.findById(fileId).map(SqlFile::toFile);
    }
}
