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

interface SqlReceiptItemRepository extends JpaRepository<SqlReceiptItem, UUID> {

    @Modifying
    @Query("""
        UPDATE SqlReceiptItem r
        SET r.product = :dict
        WHERE r.product.id IN (:productDictIdList)
        """)
    void reassignProductDict(SqlProductDictQuery dict, List<UUID> productDictIdList);

    @Query("""
        SELECT item FROM SqlReceiptItem item INNER JOIN FETCH item.parentItem parentItem 
        WHERE item.receiptRevision.id = :revisionId AND 
                item.receiptRevision.resolver = pl.madzierski.daniel.receipt.ReceiptResolverStrategyType.USER AND 
                item.product IS NULL AND 
                parentItem.product IS NULL""")
    Set<SqlReceiptItem> findUnmatchedItemsByReceiptId(UUID revisionId);

    @Modifying
    @Query("""
        UPDATE SqlReceiptItem i
        SET i.product = :product
        WHERE i.id IN :ids
        """)
    void updateProduct(SqlProductDictQuery product, Collection<UUID> ids);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM SqlReceiptItem i WHERE i.id IN :ids")
    void deleteByIds(Collection<UUID> ids);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
        UPDATE SqlReceiptItem i
        SET i.parentItem = null
        WHERE i.receiptRevision.id IN (SELECT r.id FROM SqlReceiptRevision r WHERE r.receipt.id = :receiptId)
        """)
    void detachParentItemsByReceiptId(UUID receiptId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
        DELETE FROM SqlReceiptItem i
        WHERE i.receiptRevision.id IN (SELECT r.id FROM SqlReceiptRevision r WHERE r.receipt.id = :receiptId)
        """)
    void deleteByReceiptId(UUID receiptId);

}

@AllArgsConstructor
@Repository
class ReceiptItemRepositoryImpl implements ReceiptItemRepository {

    private final SqlReceiptItemRepository receiptItemRepository;
    private final SqlReceiptRevisionRepository receiptRevisionRepository;

    @Override
    public void reassignProductDict(ProductDictQuery dict, List<UUID> productDictIdList) {
        this.receiptItemRepository.reassignProductDict(SqlProductDictQuery.fromProductDict(dict), productDictIdList);
    }

    @Override
    public void deleteAllByIdIn(List<UUID> ids) {
        if (ids.isEmpty()) return;
        this.receiptItemRepository.deleteByIds(ids);
    }

    @Override
    public void deleteAllByReceiptId(UUID receiptId) {
        this.receiptItemRepository.detachParentItemsByReceiptId(receiptId);
        this.receiptItemRepository.deleteByReceiptId(receiptId);
    }
    @Override
    public List<ReceiptItem> saveAll(List<ReceiptItem> entities, UUID receiptRevisionId) {
        SqlReceiptRevision sqlReceiptRevision =
            this.receiptRevisionRepository.findById(receiptRevisionId).orElseThrow(() -> new AppRuntimeException(AppRuntimeExceptionMessages.RECEIPT_REVISION_NOT_FOUND));
        return receiptItemRepository.saveAll(entities.stream().map(item -> SqlReceiptItem.fromReceiptItem(item,
            sqlReceiptRevision)).collect(Collectors.toSet())).stream().map(SqlReceiptItem::toReceiptItem).toList();
    }

    @Override
    public List<ReceiptItem> findUnmatchedItemsByReceiptId(UUID revisionId) {
        return receiptItemRepository.findUnmatchedItemsByReceiptId(revisionId)
            .stream()
            .map(SqlReceiptItem::toReceiptItem)
            .toList();
    }

    @Override
    public void updateProduct(ProductDictQuery product, Collection<UUID> itemIds) {
        receiptItemRepository.updateProduct(SqlProductDictQuery.fromProductDict(product), itemIds);
    }
}
