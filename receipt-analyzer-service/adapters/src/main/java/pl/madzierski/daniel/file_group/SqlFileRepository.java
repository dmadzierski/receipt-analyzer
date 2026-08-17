package pl.madzierski.daniel.file_group;

import lombok.AllArgsConstructor;
import org.springframework.data.repository.Repository;

import java.util.Optional;

interface SqlFileRepository extends Repository<SqlFile, String> {
    Optional<SqlFile> findById(String fileId);
}

interface SqlFileGroupRepository extends Repository<SqlFileGroup, String> {
    SqlFileGroup save(SqlFileGroup sqlFileGroup);
}

@AllArgsConstructor
@org.springframework.stereotype.Repository
class FileRepositoryImpl implements FileRepository {

    private final SqlFileRepository fileRepository;
    private final SqlFileGroupRepository fileGroupRepository;

    @Override
    public Optional<File> findById(String fileId) {
        return fileRepository.findById(fileId).map(SqlFile::toFile);
    }

    @Override
    public FileGroup save(FileGroup fileGroup) {
        return this.fileGroupRepository.save(SqlFileGroup.fromFileGroup(fileGroup)).toFileGroupDto();
    }
}
