package pl.madzierski.daniel.app.wallet;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WalletRepository extends JpaRepository<WalletEntity, String> {
    List<WalletEntity> findAllByUserSub(String currentUserSub);}
