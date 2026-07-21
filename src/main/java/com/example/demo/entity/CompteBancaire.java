package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Compte bancaire de l'entreprise (BNI, BOA, BFV, MCB...). Table "comptes_bancaires" créée
 * automatiquement par Hibernate au démarrage.
 */
@Entity
@Table(name = "comptes_bancaires")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompteBancaire {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** Verrou optimiste : évite qu'un solde devienne incohérent en cas de mouvements concurrents. */
  @Version private Long version;

  @NotBlank
  @Column(name = "nom_banque", nullable = false, length = 100)
  private String nomBanque;

  @Column(name = "numero_compte", length = 50)
  private String numeroCompte;

  @Column(nullable = false, precision = 16, scale = 2)
  @Builder.Default
  private BigDecimal solde = BigDecimal.ZERO;
}
