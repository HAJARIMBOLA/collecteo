package com.example.demo.controller;

import com.example.demo.dto.UtilisateurRequest;
import com.example.demo.dto.UtilisateurResponse;
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
  public List<UtilisateurResponse> listerTous() {
    return utilisateurService.listerTous().stream().map(UtilisateurResponse::depuis).toList();
  }

  @GetMapping("/{id}")
  public UtilisateurResponse trouverParId(@PathVariable Long id) {
    return UtilisateurResponse.depuis(utilisateurService.trouverParId(id));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public UtilisateurResponse creer(@Valid @RequestBody UtilisateurRequest requete) {
    return UtilisateurResponse.depuis(utilisateurService.creer(requete));
  }

  @PutMapping("/{id}/statut")
  public UtilisateurResponse changerStatut(@PathVariable Long id, @RequestParam boolean actif) {
    return UtilisateurResponse.depuis(utilisateurService.changerStatut(id, actif));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> supprimer(@PathVariable Long id) {
    utilisateurService.supprimer(id);
    return ResponseEntity.noContent().build();
  }
}
