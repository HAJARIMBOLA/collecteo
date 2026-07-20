package com.example.demo.controller;

import com.example.demo.dto.PretBancaireRequest;
import com.example.demo.entity.PretBancaire;
import com.example.demo.entity.RemboursementPret;
import com.example.demo.service.PretBancaireService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/prets")
@RequiredArgsConstructor
public class PretBancaireController {
  private final PretBancaireService pretBancaireService;

  @GetMapping
  public List<PretBancaire> listerTous() {
    return pretBancaireService.listerTous();
  }

  @GetMapping("/{id}")
  public PretBancaire trouverParId(@PathVariable Long id) {
    return pretBancaireService.trouverParId(id);
  }

  @GetMapping("/{id}/remboursements")
  public List<RemboursementPret> listerRemboursements(@PathVariable Long id) {
    return pretBancaireService.listerRemboursements(id);
  }

  @GetMapping("/resume")
  public Map<String, BigDecimal> resume() {
    return Map.of(
        "totalEmprunte",
        pretBancaireService.totalEmprunte(),
        "totalRembourse",
        pretBancaireService.totalRembourse(),
        "capitalRestant",
        pretBancaireService.capitalRestantTotal());
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public PretBancaire creer(@Valid @RequestBody PretBancaireRequest requete) {
    return pretBancaireService.creerPret(requete);
  }

  @PostMapping("/{id}/remboursement")
  public PretBancaire rembourser(@PathVariable Long id, @RequestParam BigDecimal montant) {
    return pretBancaireService.rembourser(id, montant);
  }
}
