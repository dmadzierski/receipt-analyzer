package pl.madzierski.daniel.product_dict;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface ProductDictRepository extends JpaRepository<ProductDictEntity, String> {
}
