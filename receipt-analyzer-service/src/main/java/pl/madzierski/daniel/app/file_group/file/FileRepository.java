package pl.madzierski.daniel.app.file_group.file;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface FileRepository extends JpaRepository<FileEntity, String> {
}