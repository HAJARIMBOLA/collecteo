package com.example.demo.repository;

import com.example.demo.entity.PretBancaire;
import com.example.demo.entity.enums.StatutPret;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PretBancaireRepository extends JpaRepository<PretBancaire, Long> {
  List<PretBancaire> findByStatut(StatutPret statut);

  // Charge le prêt avec son compte bancaire déjà initialisé, pour éviter une
  // LazyInitializationException quand l'entité est sérialisée en JSON après la fin de la
  // transaction (open-in-view désactivé).
  @Query("SELECT p FROM PretBancaire p JOIN FETCH p.compteBancaire WHERE p.id = :id")
  Optional<PretBancaire> findByIdAvecCompte(@Param("id") Long id);

  @Query("SELECT COALESCE(SUM(p.montantEmprunte), 0) FROM PretBancaire p")
  BigDecimal calculerTotalEmprunte();

  @Query("SELECT COALESCE(SUM(p.totalRembourse), 0) FROM PretBancaire p")
  BigDecimal calculerTotalRembourse();

  @Query("SELECT COALESCE(SUM(p.capitalRestant), 0) FROM PretBancaire p")
  BigDecimal calculerCapitalRestantTotal();
}
