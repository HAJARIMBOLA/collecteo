package com.example.demo.entity;

import com.example.demo.entity.enums.CategorieDepense;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Dépense de l'entreprise (transport, carburant, salaires, téléphone...). Chaque dépense
 * enregistrée déclenche automatiquement une SORTIE de caisse (voir DepenseService), pour que la
 * caisse et la comptabilité restent cohérentes. Table "depenses" créée automatiquement par
 * Hibernate au démarrage.
 */
@Entity
@Table(name = "depenses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Depense {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private CategorieDepense categorie;

  @Column(nullable = false, precision = 14, scale = 2)
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
