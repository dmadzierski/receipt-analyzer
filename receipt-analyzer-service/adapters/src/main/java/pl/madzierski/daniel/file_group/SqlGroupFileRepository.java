package pl.madzierski.daniel.file_group;

import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;

interface SqlFileGroupRepository extends JpaRepository<SqlFileGroup, String> {
    SqlFileGroup save(SqlFileGroup sqlFileGroup);
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
}