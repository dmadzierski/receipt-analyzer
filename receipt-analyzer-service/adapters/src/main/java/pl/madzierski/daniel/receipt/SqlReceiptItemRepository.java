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

import java.util.*;
import java.util.stream.Collectors;

interface SqlReceiptItemRepository extends JpaRepository<SqlReceiptItem, String> {

    @Modifying
    @Query("""
        UPDATE SqlReceiptItem r
        SET r.product = :dict
        WHERE r.product.id IN (:productDictIdList)
        """)
    void reassignProductDict(SqlProductDictQuery dict, List<String> productDictIdList);

    @Query("""
        SELECT item FROM SqlReceiptItem item LEFT JOIN FETCH item.parentItem parentItem 
        WHERE item.receiptRevision.id = :revisionId AND 
                item.receiptRevision.resolver = pl.madzierski.daniel.receipt.ReceiptResolverStrategyType.USER AND 
                parentItem.receiptRevision.resolver <> pl.madzierski.daniel.receipt.ReceiptResolverStrategyType.USER AND 
                item.product IS NULL AND 
                parentItem.product IS NULL""")
    Set<SqlReceiptItem> findByIdWithItemsAndItemsParentWhenRevisionResolverIsUserAndParentRevisionResolverIsNotAndProductIsEmpty(String revisionId);

    @Modifying
    @Query("""
        UPDATE SqlReceiptItem i
        SET i.product = :product
        WHERE i.id IN :ids
        """)
    void updateProduct(SqlProductDictQuery product, Collection<String> ids);

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
        return receiptItemRepository.saveAll(entities.stream().map(item -> SqlReceiptItem.fromReceiptItem(item,
            sqlReceiptRevision)).collect(Collectors.toSet())).stream().map(SqlReceiptItem::toReceiptItem).toList();
    }

    @Override
    public List<ReceiptItem> findByRevisionIdWithParentItemWhenRevisionResolverIsUserAndParentRevisionResolverIsNotAndProductIsEmpty(String revisionId) {
        return receiptItemRepository.findByIdWithItemsAndItemsParentWhenRevisionResolverIsUserAndParentRevisionResolverIsNotAndProductIsEmpty(revisionId)
            .stream()
            .map(SqlReceiptItem::toReceiptItem)
            .toList();
    }

    @Override
    public void updateProduct(ProductDictQuery product, Collection<String> itemIds) {
        receiptItemRepository.updateProduct(SqlProductDictQuery.fromProductDict(product), itemIds);
    }
}
