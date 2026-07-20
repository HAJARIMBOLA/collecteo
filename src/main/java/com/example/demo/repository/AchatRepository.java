package com.example.demo.repository;

import com.example.demo.entity.Achat;
import com.example.demo.entity.enums.StatutPaiement;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AchatRepository extends JpaRepository<Achat, Long> {
  List<Achat> findByProducteurIdOrderByDateAchatDesc(Long producteurId);

  List<Achat> findByStatutOrderByDateAchatDesc(StatutPaiement statut);

  // Charge l'achat avec son producteur et son produit déjà initialisés, pour éviter une
  // LazyInitializationException quand l'entité est sérialisée en JSON après la fin de la
  // transaction (open-in-view désactivé).
  @Query("SELECT a FROM Achat a JOIN FETCH a.producteur JOIN FETCH a.produit WHERE a.id = :id")
  Optional<Achat> findByIdAvecRelations(@Param("id") Long id);

  // SELECT * FROM achats WHERE producteur_id = :id AND montant_restant > 0
  @Query("SELECT a FROM Achat a WHERE a.producteur.id = :producteurId AND a.montantRestant > 0")
  List<Achat> trouverDettesParProducteur(@Param("producteurId") Long producteurId);

  // Total des dettes (= ce que le collecteur doit encore aux producteurs)
  @Query("SELECT COALESCE(SUM(a.montantRestant), 0) FROM Achat a")
  BigDecimal calculerTotalDettes();

  // Total de tous les achats (chiffre d'affaires achats) sur une période
  @Query(
      "SELECT COALESCE(SUM(a.montantTotal), 0) FROM Achat a WHERE a.dateAchat BETWEEN :debut "
          + "AND :fin")
  BigDecimal calculerTotalAchatsEntre(
      @Param("debut") java.time.LocalDateTime debut, @Param("fin") java.time.LocalDateTime fin);

  @Query("SELECT COALESCE(SUM(a.montantTotal), 0) FROM Achat a")
  BigDecimal calculerTotalAchats();
}
