package hei.school.add.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Producteur auprès duquel le collecteur achète des produits. Table "producteurs" créée
 * automatiquement par Hibernate au démarrage.
 */
@Entity
@Table(name = "producteurs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producteur {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  @Column(nullable = false, length = 150)
  private String nom;

  @Column(length = 30)
  private String telephone;

  @Column(length = 255)
  private String adresse;

  @Column(name = "date_creation", updatable = false)
  private LocalDateTime dateCreation;

  @PrePersist
  protected void onCreate() {
    this.dateCreation = LocalDateTime.now();
  }
}
