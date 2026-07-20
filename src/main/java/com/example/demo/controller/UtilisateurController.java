package com.example.demo.controller;

import com.example.demo.dto.UtilisateurRequest;
import com.example.demo.entity.Utilisateur;
import com.example.demo.service.UtilisateurService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Réservé aux ADMINISTRATEUR (voir SecurityConfig). */
@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
public class UtilisateurController {
  private final UtilisateurService utilisateurService;

  @GetMapping
  public List<Utilisateur> listerTous() {
    return utilisateurService.listerTous();
  }

  @GetMapping("/{id}")
  public Utilisateur trouverParId(@PathVariable Long id) {
    return utilisateurService.trouverParId(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Utilisateur creer(@Valid @RequestBody UtilisateurRequest requete) {
    return utilisateurService.creer(requete);
  }

  @PutMapping("/{id}/statut")
  public Utilisateur changerStatut(@PathVariable Long id, @RequestParam boolean actif) {
    return utilisateurService.changerStatut(id, actif);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> supprimer(@PathVariable Long id) {
    utilisateurService.supprimer(id);
    return ResponseEntity.noContent().build();
  }
}
