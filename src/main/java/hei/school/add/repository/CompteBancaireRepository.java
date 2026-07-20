package hei.school.add.repository;

import hei.school.add.entity.CompteBancaire;
import java.math.BigDecimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CompteBancaireRepository extends JpaRepository<CompteBancaire, Long> {
  // SELECT COALESCE(SUM(solde),0) FROM comptes_bancaires
  @Query("SELECT COALESCE(SUM(c.solde), 0) FROM CompteBancaire c")
  BigDecimal calculerSoldeTotalBanques();
}
