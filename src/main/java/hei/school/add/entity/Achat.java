package hei.school.add.entity;

import hei.school.add.entity.enums.StatutPaiement;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Achat effectué par le collecteur auprès d'un producteur. Table "achats" créée automatiquement par
 * Hibernate au démarrage.
 *
 * <p>montantRestant représente une DETTE du collecteur envers le producteur tant qu'il n'est pas à
 * zéro.
 */
@Entity
@Table(name = "achats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Achat {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "producteur_id", nullable = false)
  private Producteur producteur;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "produit_id", nullable = false)
  private Produit produit;

  @Column(nullable = false, precision = 14, scale = 2)
  private BigDecimal quantite;

  @Column(name = "prix_unitaire", nullable = false, precision = 14, scale = 2)
  private BigDecimal prixUnitaire;

  @Column(name = "montant_total", nullable = false, precision = 14, scale = 2)
  private BigDecimal montantTotal;

  @Column(name = "montant_paye", nullable = false, precision = 14, scale = 2)
  @Builder.Default
  private BigDecimal montantPaye = BigDecimal.ZERO;

  @Column(name = "montant_restant", nullable = false, precision = 14, scale = 2)
  @Builder.Default
  private BigDecimal montantRestant = BigDecimal.ZERO;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 25)
  private StatutPaiement statut;

  @Column(name = "date_achat", nullable = false)
  private LocalDateTime dateAchat;

  @PrePersist
  protected void onCreate() {
    if (this.dateAchat == null) {
      this.dateAchat = LocalDateTime.now();
    }
  }
}
