package pl.madzierski.daniel.app.receipt.revision.item;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ReceiptItemProvider {

    private final ReceiptItemRepository receiptItemRepository;

    public List<ReceiptItemEntity> saveAll(Collection<ReceiptItemEntity> items) {
        return receiptItemRepository.saveAll(items);
    }

    public Optional<ReceiptItemEntity> findById(String id) {
        return receiptItemRepository.findById(id);
    }

    public ReceiptItemEntity save(ReceiptItemEntity item) {
        return receiptItemRepository.save(item);
    }

    public List<ReceiptItemEntity> findAllMissingAliasesInRevision(String revisionId) {
        return receiptItemRepository.findAllMissingAliasesInRevision(revisionId);
    }
}