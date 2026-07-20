package com.example.demo.entity;

import com.example.demo.entity.enums.RoleUtilisateur;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Utilisateur de l'application (Administrateur, Collecteur, Comptable, Caissier, Responsable). Le
 * mot de passe n'est JAMAIS stocké en clair : il est haché avec BCrypt avant sauvegarde (voir
 * UtilisateurService.creer()). Table "utilisateurs" créée automatiquement par Hibernate au
 * démarrage.
 */
@Entity
@Table(name = "utilisateurs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Utilisateur {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  @Column(name = "nom_utilisateur", nullable = false, unique = true, length = 60)
  private String nomUtilisateur;

  /** Hash BCrypt, jamais le mot de passe en clair. */
  @NotBlank
  @Column(name = "mot_de_passe", nullable = false)
  private String motDePasse;

  @NotBlank
  @Column(nullable = false, length = 150)
  private String nom;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 25)
  private RoleUtilisateur role;

  @Column(nullable = false)
  @Builder.Default
  private boolean actif = true;

  @Column(name = "date_creation", updatable = false)
  private LocalDateTime dateCreation;

  @PrePersist
  protected void onCreate() {
    this.dateCreation = LocalDateTime.now();
  }
}
