package hei.school.add.repository;

import hei.school.add.entity.BanqueTransaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BanqueTransactionRepository extends JpaRepository<BanqueTransaction, Long> {
  List<BanqueTransaction> findByCompteBancaireIdOrderByDateDesc(Long compteBancaireId);
}
