package pl.madzierski.daniel.receipt;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.madzierski.daniel.product_dict.model.ProductDictQueryEntity;

import java.util.List;

@Repository
interface ReceiptItemRepository extends JpaRepository<ReceiptItemEntity, String> {

    @Modifying
    @Query("""
        UPDATE ReceiptItemEntity r
        SET r.nameDict = :dict
        WHERE r.nameDict.id IN (:productDictIdList)
        """)
    void reassignProductDict(ProductDictQueryEntity dict, List<String> productDictIdList);

}
