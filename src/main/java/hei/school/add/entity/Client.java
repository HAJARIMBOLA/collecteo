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
 * Client à qui le collecteur vend des produits. Table "clients" créée automatiquement par Hibernate
 * au démarrage.
 */
@Entity
@Table(name = "clients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client {
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
