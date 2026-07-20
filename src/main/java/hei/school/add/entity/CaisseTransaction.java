package hei.school.add.entity;

import hei.school.add.entity.enums.CategorieTransactionCaisse;
import hei.school.add.entity.enums.TypeTransactionCaisse;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Mouvement de caisse (entrée ou sortie d'argent liquide). Table "caisse_transactions" créée
 * automatiquement par Hibernate au démarrage.
 *
 * <p>Alimentée automatiquement par AchatService et VenteService lors des paiements, et manuellement
 * via CaisseController pour les apports, salaires, carburant, etc.
 */
@Entity
@Table(name = "caisse_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaisseTransaction {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private TypeTransactionCaisse type;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private CategorieTransactionCaisse categorie;

  @Column(nullable = false, precision = 14, scale = 2)
  private BigDecimal montant;

  @Column(length = 255)
  private String description;

  /** Référence facultative vers un Achat ou une Vente à l'origine de cette transaction. */
  @Column(name = "reference_id")
  private Long referenceId;

  @Column(nullable = false)
  private LocalDateTime date;

  @PrePersist
  protected void onCreate() {
    if (this.date == null) {
      this.date = LocalDateTime.now();
    }
  }
}
