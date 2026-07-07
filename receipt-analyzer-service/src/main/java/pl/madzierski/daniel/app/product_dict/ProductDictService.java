package pl.madzierski.daniel.app.product_dict;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
class ProductDictService {

    private final ProductDictRepository productDictRepository;
    private final ReceiptItemEntityProvider receiptItemEntityProvider;

//    public void updateDictByUserRevision(String revisionId) {
//        List<ReceiptItemEntity> allNotExistAliases = receiptItemEntityProvider.findAllMissingAliasesInRevision(revisionId);
//        allNotExistAliases.forEach(receiptItemEntity ->
//                save(receiptItemEntity.getName(), receiptItemEntity.getParentItem().getName()));
//    }
//
//    private void save(String canonicalName, String alias) {
//        productDictRepository.findProductDictEntityByName(canonicalName)
//                .ifPresentOrElse(
//                        productDictEntity -> productDictEntity.addAlias(alias),
//                        () -> productDictRepository.save(ProductDictEntity.builder().name(canonicalName).aliases(Set.of(alias, canonicalName)).build()));
//
//    }
}
