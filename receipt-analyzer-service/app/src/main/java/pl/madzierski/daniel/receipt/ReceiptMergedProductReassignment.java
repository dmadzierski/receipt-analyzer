package pl.madzierski.daniel.receipt;

import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import pl.madzierski.daniel.product_dict.MergedProductReassignment;
import pl.madzierski.daniel.product_dict.model.ProductDictQuery;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
class ReceiptMergedProductReassignment implements MergedProductReassignment {

    private final ReceiptRevisionRepository revisionRepository;

    @Override
    @Transactional
    public void reassignToPrimary(ProductDictQuery primaryProduct, List<UUID> mergedProductIds) {
        revisionRepository.reassignProductDict(primaryProduct, mergedProductIds);
    }
}
