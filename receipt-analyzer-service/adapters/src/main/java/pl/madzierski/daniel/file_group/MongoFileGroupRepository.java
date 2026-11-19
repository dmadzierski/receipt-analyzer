package pl.madzierski.daniel.file_group;

import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import pl.madzierski.daniel.file_group.model.FileGroupDto;

import java.util.List;
import java.util.UUID;

interface MongoFileGroupRepository extends MongoRepository<MongoFileGroup, UUID> {
    List<MongoFileGroup> findAllByReceiptId(UUID receiptId);

    void deleteAllByReceiptId(UUID receiptId);
}

@Repository
@RequiredArgsConstructor
class FileGroupQueryRepositoryImpl implements FileGroupQueryRepository {

    private final MongoFileGroupRepository repository;

    @Override
    public List<FileGroupDto> findAllByReceiptId(UUID receiptId) {
        return repository.findAllByReceiptId(receiptId).stream().map(MongoFileGroup::toDto).toList();
    }
}
