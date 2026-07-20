package com.example.demo.entity;

import com.example.demo.entity.enums.StatutPret;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Prêt bancaire contracté par l'entreprise. Table "prets_bancaires" créée automatiquement par
 * Hibernate au démarrage.
 */
@Entity
@Table(name = "prets_bancaires")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PretBancaire {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "compte_bancaire_id", nullable = false)
  private CompteBancaire compteBancaire;

  @Column(name = "montant_emprunte", nullable = false, precision = 16, scale = 2)
  private BigDecimal montantEmprunte;

  @Column(name = "date_pret", nullable = false)
  private LocalDate datePret;

  @Column(name = "duree_mois", nullable = false)
  private Integer dureeMois;

  /** Taux d'intérêt annuel en pourcentage, ex : 12.5 pour 12.5%. */
  @Column(name = "taux_interet", nullable = false, precision = 6, scale = 3)
  private BigDecimal tauxInteret;

  @Column(nullable = false, precision = 16, scale = 2)
  private BigDecimal mensualite;

  @Column(name = "capital_restant", nullable = false, precision = 16, scale = 2)
  private BigDecimal capitalRestant;

  @Column(name = "total_rembourse", nullable = false, precision = 16, scale = 2)
  @Builder.Default
  private BigDecimal totalRembourse = BigDecimal.ZERO;

  @Column(name = "prochaine_echeance")
  private LocalDate prochaineEcheance;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 15)
  private StatutPret statut;
}
