package com.example.demo.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Échéance / remboursement payé sur un prêt bancaire. Table "remboursements_prets" créée
 * automatiquement par Hibernate au démarrage.
 */
@Entity
@Table(name = "remboursements_prets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RemboursementPret {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "pret_id", nullable = false)
  private PretBancaire pret;

  @Column(nullable = false, precision = 16, scale = 2)
  private BigDecimal montant;

  @Column(nullable = false)
  private LocalDateTime date;

  @PrePersist
  protected void onCreate() {
    if (this.date == null) {
      this.date = LocalDateTime.now();
    }
  }
}
