package hei.school.add.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Produit géré par le collecteur (ex : café, vanille, riz...). Table "produits" créée
 * automatiquement par Hibernate au démarrage.
 *
 * <p>prixMoyenAchat et prixMoyenVente sont recalculés par AchatService / VenteService à chaque
 * nouvel achat ou vente (moyenne pondérée), ce qui évite de devoir les gérer manuellement.
 */
@Entity
@Table(name = "produits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Produit {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  @Column(nullable = false, length = 150)
  private String nom;

  @Column(length = 100)
  private String categorie;

  @Column(name = "prix_moyen_achat", precision = 14, scale = 2)
  @Builder.Default
  private BigDecimal prixMoyenAchat = BigDecimal.ZERO;

  @Column(name = "prix_moyen_vente", precision = 14, scale = 2)
  @Builder.Default
  private BigDecimal prixMoyenVente = BigDecimal.ZERO;

  @Column(name = "quantite_stock", precision = 14, scale = 2)
  @Builder.Default
  private BigDecimal quantiteStock = BigDecimal.ZERO;

  /** Unité de mesure du produit : kg, sac, litre, unité... */
  @Column(length = 20)
  @Builder.Default
  private String unite = "kg";

  /** Valeur du stock = quantiteStock * prixMoyenAchat (calculée à la volée, jamais stockée). */
  @Transient
  public BigDecimal getValeurStock() {
    if (quantiteStock == null || prixMoyenAchat == null) return BigDecimal.ZERO;
    return quantiteStock.multiply(prixMoyenAchat);
  }
}
