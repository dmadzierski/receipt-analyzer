package pl.madzierski.daniel.app.receipt;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@AllArgsConstructor
@Service
public class ReceiptProvider {

    private final ReceiptRepository receiptRepository;

    public Optional<ReceiptEntity> findById(String receiptId) {
        return this.receiptRepository.findById(receiptId);
    }
}