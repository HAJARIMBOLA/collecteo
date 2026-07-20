package com.example.demo.controller;

import com.example.demo.entity.Produit;
import com.example.demo.service.ProduitService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/produits")
@RequiredArgsConstructor
public class ProduitController {
  private final ProduitService produitService;

  @GetMapping
  public List<Produit> listerTous() {
    return produitService.listerTous();
  }

  @GetMapping("/{id}")
  public Produit trouverParId(@PathVariable Long id) {
    return produitService.trouverParId(id);
  }

  @GetMapping("/rupture")
  public List<Produit> produitsEnRupture(@RequestParam(defaultValue = "0") BigDecimal seuil) {
    return produitService.produitsEnRupture(seuil);
  }

  @GetMapping("/valeur-stock")
  public BigDecimal valeurTotaleStock() {
    return produitService.valeurTotaleStock();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Produit creer(@Valid @RequestBody Produit produit) {
    return produitService.creer(produit);
  }

  @PutMapping("/{id}")
  public Produit modifier(@PathVariable Long id, @Valid @RequestBody Produit produit) {
    return produitService.modifier(id, produit);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> supprimer(@PathVariable Long id) {
    produitService.supprimer(id);
    return ResponseEntity.noContent().build();
  }
}
