package pl.madzierski.daniel.app.receipt.revision.item;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceiptItemRepository extends JpaRepository<ReceiptItemEntity, String> {
}
