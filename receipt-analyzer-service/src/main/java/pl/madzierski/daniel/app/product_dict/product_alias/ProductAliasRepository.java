package pl.madzierski.daniel.app.product_dict.product_alias;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
interface ProductAliasRepository extends JpaRepository<ProductAliasEntity, String> {

    @Query(value = """
            SELECT * FROM `receipt-analyzer-service`.`product_alias` pa
            WHERE pa.product_dict_id IN (:productDictIdList)
            """, nativeQuery = true)
    List<ProductAliasEntity> findAllByProductDictList(List<String> productDictIdList);
}
