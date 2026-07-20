package com.example.demo.entity;

import com.example.demo.entity.enums.StatutPaiement;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Vente effectuée par le collecteur à un client. Table "ventes" créée automatiquement par Hibernate
 * au démarrage.
 *
 * <p>montantRestant représente une CREANCE : l'argent que le client doit encore au collecteur tant
 * qu'il n'est pas à zéro.
 */
@Entity
@Table(name = "ventes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vente {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "client_id", nullable = false)
  private Client client;

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

  @Column(name = "date_vente", nullable = false)
  private LocalDateTime dateVente;

  @PrePersist
  protected void onCreate() {
    if (this.dateVente == null) {
      this.dateVente = LocalDateTime.now();
    }
  }
}
