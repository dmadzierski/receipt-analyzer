package pl.madzierski.daniel.product_dict;

import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;

interface SqlProductAliasRepository extends Repository<SqlProductAlias, String> {

    @Query(value = """
        UPDATE product
        SET product_id = :targetProductDictId
        WHERE product_id IN (:productDictIdsToMerge)
        """, nativeQuery = true)
    void reassignAliasesToProductDict(String targetProductDictId, List<String> productDictIdsToMerge);
}

@org.springframework.stereotype.Repository
@AllArgsConstructor
class ProductAliasRepositoryImpl implements ProductAliasRepository {

    private final SqlProductAliasRepository sqlProductAliasRepository;

    @Override
    public void reassignAliasesToProductDict(String targetProductDictId, List<String> productDictIdsToMerge) {
        sqlProductAliasRepository.reassignAliasesToProductDict(targetProductDictId, productDictIdsToMerge);
    }
}