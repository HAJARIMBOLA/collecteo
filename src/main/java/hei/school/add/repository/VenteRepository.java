package hei.school.add.repository;

import hei.school.add.entity.Vente;
import hei.school.add.entity.enums.StatutPaiement;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VenteRepository extends JpaRepository<Vente, Long> {
  List<Vente> findByClientIdOrderByDateVenteDesc(Long clientId);

  List<Vente> findByStatutOrderByDateVenteDesc(StatutPaiement statut);

  // SELECT * FROM ventes WHERE client_id = :id AND montant_restant > 0
  @Query("SELECT v FROM Vente v WHERE v.client.id = :clientId AND v.montantRestant > 0")
  List<Vente> trouverCreancesParClient(@Param("clientId") Long clientId);

  // Total des créances (= ce que les clients doivent encore au collecteur)
  @Query("SELECT COALESCE(SUM(v.montantRestant), 0) FROM Vente v")
  BigDecimal calculerTotalCreances();

  @Query(
      "SELECT COALESCE(SUM(v.montantTotal), 0) FROM Vente v WHERE v.dateVente BETWEEN :debut "
          + "AND :fin")
  BigDecimal calculerTotalVentesEntre(
      @Param("debut") java.time.LocalDateTime debut, @Param("fin") java.time.LocalDateTime fin);

  @Query("SELECT COALESCE(SUM(v.montantTotal), 0) FROM Vente v")
  BigDecimal calculerTotalVentes();
}
