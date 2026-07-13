package pl.madzierski.daniel.product_dict;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
interface ProductAliasRepository extends JpaRepository<ProductAliasEntity, String> {

    @Modifying
    @Query(value = """
        UPDATE product_alias
        SET product_dict_id = :targetProductDictId
        WHERE product_dict_id IN (:productDictIdsToMerge)
        """, nativeQuery = true)
    void reassignAliasesToProductDict(String targetProductDictId,
                                      List<String> productDictIdsToMerge);
}
