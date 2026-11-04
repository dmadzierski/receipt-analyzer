package pl.madzierski.daniel.receipt;

import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.madzierski.daniel.product_dict.SqlProductDictQuery;
import pl.madzierski.daniel.product_dict.model.ProductDictQuery;

import java.util.*;

interface SqlReceiptItemRepository extends JpaRepository<SqlReceiptItem, UUID> {

    @Modifying
    @Query("""
        UPDATE SqlReceiptItem r
        SET r.product = :dict
        WHERE r.product.id IN (:productDictIdList)
        """)
    void reassignProductDict(SqlProductDictQuery dict, List<UUID> productDictIdList);
}

@AllArgsConstructor
@Repository
class ReceiptItemRepositoryImpl implements ReceiptItemRepository {

    private final SqlReceiptItemRepository receiptItemRepository;

    @Override
    public void reassignProductDict(ProductDictQuery dict, List<UUID> productDictIdList) {
        this.receiptItemRepository.reassignProductDict(SqlProductDictQuery.fromProductDict(dict), productDictIdList);
    }
}
