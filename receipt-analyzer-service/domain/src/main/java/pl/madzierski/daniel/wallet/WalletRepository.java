package pl.madzierski.daniel.wallet;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface WalletRepository extends JpaRepository<WalletEntity, String> {
}
