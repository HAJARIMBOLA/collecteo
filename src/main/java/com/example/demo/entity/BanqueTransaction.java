package com.example.demo.entity;

import com.example.demo.entity.enums.TypeTransactionCaisse;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Mouvement sur un compte bancaire (dépôt, retrait, virement, remboursement de prêt...). Table
 * "banque_transactions" créée automatiquement par Hibernate au démarrage.
 */
@Entity
@Table(name = "banque_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BanqueTransaction {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "compte_bancaire_id", nullable = false)
  private CompteBancaire compteBancaire;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private TypeTransactionCaisse type;

  @Column(nullable = false, precision = 16, scale = 2)
  private BigDecimal montant;

  @Column(length = 255)
  private String description;

  @Column(nullable = false)
  private LocalDateTime date;

  @PrePersist
  protected void onCreate() {
    if (this.date == null) {
      this.date = LocalDateTime.now();
    }
  }
}
