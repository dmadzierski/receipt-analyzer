package pl.madzierski.daniel.file_group;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface FileGroupRepository extends JpaRepository<FileGroupEntity, Long> {
}