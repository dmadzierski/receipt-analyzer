package pl.madzierski.daniel.receipt;

import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.madzierski.daniel.product_dict.model.ProductDictQueryEntity;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

interface SqlReceiptItemRepository extends JpaRepository<SqlReceiptItem, String> {

    @Modifying
    @Query("""
        UPDATE SqlReceiptItem r
        SET r.nameDict = :dict
        WHERE r.nameDict.id IN (:productDictIdList)
        """)
    void reassignProductDict(ProductDictQueryEntity dict, List<String> productDictIdList);

}

@AllArgsConstructor
@Repository
class ReceiptItemRepositoryImpl implements ReceiptItemRepository {

    private final SqlReceiptItemRepository repository;

    @Override
    public void reassignProductDict(ProductDictQueryEntity dict, List<String> productDictIdList) {
        this.repository.reassignProductDict(dict, productDictIdList);
    }

    @Override
    public ReceiptItem save(ReceiptItem item) {
        return this.repository.save(SqlReceiptItem.fromReceiptItem(item)).toReceiptItem();
    }

    @Override
    public void deleteAllByIdIn(List<String> ids) {
        this.repository.deleteAllById(ids);
    }

    @Override
    public <S extends ReceiptItem> List<S> saveAll(Iterable<S> entities) {
        return (List<S>) this.repository.saveAll(
                StreamSupport.stream(entities.spliterator(), false)
                    .map(SqlReceiptItem::fromReceiptItem)
                    .collect(Collectors.toList())
            ).stream()
            .map(SqlReceiptItem::toReceiptItem)
            .collect(Collectors.toList());
    }
}
