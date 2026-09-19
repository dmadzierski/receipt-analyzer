package pl.madzierski.daniel.file_group;

import org.springframework.data.repository.Repository;

interface SqlFileGroupRepository extends Repository<SqlFileGroup, String> {
    SqlFileGroup save(SqlFileGroup sqlFileGroup);
}