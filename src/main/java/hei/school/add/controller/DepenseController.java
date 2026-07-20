package hei.school.add.controller;

import hei.school.add.dto.DepenseRequest;
import hei.school.add.entity.Depense;
import hei.school.add.entity.enums.CategorieDepense;
import hei.school.add.service.DepenseService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/depenses")
@RequiredArgsConstructor
public class DepenseController {
  private final DepenseService depenseService;

  @GetMapping
  public List<Depense> listerToutes() {
    return depenseService.listerToutes();
  }

  @GetMapping("/{id}")
  public Depense trouverParId(@PathVariable Long id) {
    return depenseService.trouverParId(id);
  }

  @GetMapping("/categorie/{categorie}")
  public List<Depense> listerParCategorie(@PathVariable CategorieDepense categorie) {
    return depenseService.listerParCategorie(categorie);
  }

  @GetMapping("/total")
  public BigDecimal totalDepenses() {
    return depenseService.totalDepenses();
  }

  @GetMapping("/par-categorie")
  public Map<String, BigDecimal> totalParCategorie() {
    return depenseService.totalParCategorie();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Depense creer(@Valid @RequestBody DepenseRequest requete) {
    return depenseService.creer(requete);
  }
}
