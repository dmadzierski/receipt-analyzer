package pl.madzierski.daniel.receipt;

import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.madzierski.daniel.exception.AppRuntimeException;
import pl.madzierski.daniel.exception.AppRuntimeExceptionMessages;
import pl.madzierski.daniel.product_dict.SqlProductDictQuery;
import pl.madzierski.daniel.product_dict.model.ProductDictQuery;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

interface SqlReceiptItemRepository extends JpaRepository<SqlReceiptItem, String> {

    @Modifying
    @Query("""
        UPDATE SqlReceiptItem r
        SET r.product = :dict
        WHERE r.product.id IN (:productDictIdList)
        """)
    void reassignProductDict(SqlProductDictQuery dict, List<String> productDictIdList);

}

@AllArgsConstructor
@Repository
class ReceiptItemRepositoryImpl implements ReceiptItemRepository {

    private final SqlReceiptItemRepository receiptItemRepository;
    private final SqlReceiptRevisionRepository receiptRevisionRepository;

    @Override
    public void reassignProductDict(ProductDictQuery dict, List<String> productDictIdList) {
        this.receiptItemRepository.reassignProductDict(SqlProductDictQuery.fromProductDict(dict), productDictIdList);
    }

    @Override
    public void deleteAllByIdIn(List<String> ids) {
        this.receiptItemRepository.deleteAllById(ids);
    }

    @Override
    public List<ReceiptItem> saveAll(List<ReceiptItem> entities, String receiptRevisionId) {
        SqlReceiptRevision sqlReceiptRevision =
            this.receiptRevisionRepository.findById(receiptRevisionId).orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.RECEIPT_REVISION_NOT_FOUND));
        this.receiptItemRepository.flush();
        return receiptItemRepository.saveAll(entities.stream().map(item -> SqlReceiptItem.fromReceiptItem(item,
            sqlReceiptRevision)).collect(Collectors.toSet())).stream().map(SqlReceiptItem::toReceiptItem).toList();
    }
}
