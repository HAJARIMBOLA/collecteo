package com.example.demo.dto;

import com.example.demo.entity.Utilisateur;
import com.example.demo.entity.enums.RoleUtilisateur;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

/**
 * Représentation d'un utilisateur renvoyée par l'API — ne contient JAMAIS le mot de passe (même
 * hashé). Utilisé à la place de l'entité Utilisateur dans toutes les réponses de
 * UtilisateurController pour éviter d'exposer le hash BCrypt.
 */
@Getter
@Builder
public class UtilisateurResponse {
  private Long id;
  private String nomUtilisateur;
  private String nom;
  private RoleUtilisateur role;
  private boolean actif;
  private LocalDateTime dateCreation;

  public static UtilisateurResponse depuis(Utilisateur utilisateur) {
    return UtilisateurResponse.builder()
        .id(utilisateur.getId())
        .nomUtilisateur(utilisateur.getNomUtilisateur())
        .nom(utilisateur.getNom())
        .role(utilisateur.getRole())
        .actif(utilisateur.isActif())
        .dateCreation(utilisateur.getDateCreation())
        .build();
  }
}
