package hei.school.add.controller;

import hei.school.add.entity.Producteur;
import hei.school.add.service.ProducteurService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/producteurs")
@RequiredArgsConstructor
public class ProducteurController {
  private final ProducteurService producteurService;

  @GetMapping
  public List<Producteur> listerTous() {
    return producteurService.listerTous();
  }

  @GetMapping("/{id}")
  public Producteur trouverParId(@PathVariable Long id) {
    return producteurService.trouverParId(id);
  }

  @GetMapping("/recherche")
  public List<Producteur> rechercher(@RequestParam String motCle) {
    return producteurService.rechercher(motCle);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Producteur creer(@Valid @RequestBody Producteur producteur) {
    return producteurService.creer(producteur);
  }

  @PutMapping("/{id}")
  public Producteur modifier(@PathVariable Long id, @Valid @RequestBody Producteur producteur) {
    return producteurService.modifier(id, producteur);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> supprimer(@PathVariable Long id) {
    producteurService.supprimer(id);
    return ResponseEntity.noContent().build();
  }
}
