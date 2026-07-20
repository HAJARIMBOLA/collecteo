package hei.school.add.entity;

import hei.school.add.entity.enums.StatutPaiement;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Dette envers un fournisseur ou un partenaire (hors achats de produits aux producteurs, qui sont
 * déjà suivis via l'entité Achat). Exemple : facture de réparation, prestataire, etc. Table
 * "dettes" créée automatiquement par Hibernate au démarrage.
 */
@Entity
@Table(name = "dettes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dette {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  @Column(nullable = false, length = 150)
  private String fournisseur;

  @Column(length = 255)
  private String motif;

  @Column(nullable = false, precision = 14, scale = 2)
  private BigDecimal montant;

  @Column(name = "montant_paye", nullable = false, precision = 14, scale = 2)
  @Builder.Default
  private BigDecimal montantPaye = BigDecimal.ZERO;

  @Column(name = "montant_restant", nullable = false, precision = 14, scale = 2)
  private BigDecimal montantRestant;

  @Column(name = "date_limite")
  private LocalDate dateLimite;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 25)
  private StatutPaiement statut;

  @Column(name = "date_creation", updatable = false)
  private LocalDateTime dateCreation;

  @PrePersist
  protected void onCreate() {
    this.dateCreation = LocalDateTime.now();
    if (this.montantRestant == null) {
      this.montantRestant =
          this.montant.subtract(this.montantPaye == null ? BigDecimal.ZERO : this.montantPaye);
    }
  }
}
