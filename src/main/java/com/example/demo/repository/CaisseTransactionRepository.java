package com.example.demo.repository;

import com.example.demo.entity.CaisseTransaction;
import com.example.demo.entity.enums.TypeTransactionCaisse;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CaisseTransactionRepository extends JpaRepository<CaisseTransaction, Long> {
  List<CaisseTransaction> findAllByOrderByDateDesc();

  List<CaisseTransaction> findByTypeOrderByDateDesc(TypeTransactionCaisse type);

  // SELECT COALESCE(SUM(montant),0) FROM caisse_transactions WHERE type = 'ENTREE'
  @Query("SELECT COALESCE(SUM(c.montant), 0) FROM CaisseTransaction c WHERE c.type = :type")
  BigDecimal calculerTotalParType(@Param("type") TypeTransactionCaisse type);

  // Solde de caisse = total entrées - total sorties
  @Query(
      """
      SELECT
        COALESCE(SUM(CASE WHEN c.type = 'ENTREE' THEN c.montant ELSE 0 END), 0) -
        COALESCE(SUM(CASE WHEN c.type = 'SORTIE' THEN c.montant ELSE 0 END), 0)
      FROM CaisseTransaction c
      """)
  BigDecimal calculerSoldeCaisse();
}
