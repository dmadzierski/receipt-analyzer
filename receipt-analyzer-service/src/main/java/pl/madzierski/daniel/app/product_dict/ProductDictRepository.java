package pl.madzierski.daniel.app.product_dict;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ProductDictRepository extends JpaRepository<ProductDictEntity, String> {

    @Query("""
            SELECT d FROM ProductDictEntity d 
            LEFT JOIN FETCH d.aliases a 
            WHERE EXISTS (
                 SELECT 1 FROM ProductAliasEntity a
                 WHERE a.productDict = d AND a.name = :alias
            )
            """)
    Optional<ProductDictEntity> findByAlias(String alias);

    @Cacheable("allDictionaries")
    @Query("SELECT d FROM ProductDictEntity d LEFT JOIN FETCH d.aliases")
    Set<ProductDictEntity> findAllCacheable();

    @Override
    @CacheEvict(value = "allDictionaries", allEntries = true)
    <S extends ProductDictEntity> S save(S entity);

    @Override
    @CacheEvict(value = "allDictionaries", allEntries = true)
    <S extends ProductDictEntity> List<S> saveAll(Iterable<S> entities);

    long countByProductCategory_Id(String productCategoryId);
}
