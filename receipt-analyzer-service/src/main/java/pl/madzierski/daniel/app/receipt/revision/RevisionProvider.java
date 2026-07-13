package pl.madzierski.daniel.app.receipt.revision;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class RevisionProvider {

    private final ReceiptRevisionRepository revisionRepository;

    public List<ReceiptRevisionEntity> getReceiptRevisions(String receiptId) {
        return revisionRepository.findReceiptRevisionEntityByReceiptId(receiptId);
    }
}