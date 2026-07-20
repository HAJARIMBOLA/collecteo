package com.example.demo.controller;

import com.example.demo.dto.AchatRequest;
import com.example.demo.dto.PaiementRequest;
import com.example.demo.entity.Achat;
import com.example.demo.service.AchatService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/achats")
@RequiredArgsConstructor
public class AchatController {
  private final AchatService achatService;

  @GetMapping
  public List<Achat> listerTous() {
    return achatService.listerTous();
  }

  @GetMapping("/{id}")
  public Achat trouverParId(@PathVariable Long id) {
    return achatService.trouverParId(id);
  }

  @GetMapping("/producteur/{producteurId}")
  public List<Achat> listerParProducteur(@PathVariable Long producteurId) {
    return achatService.listerParProducteur(producteurId);
  }

  @GetMapping("/producteur/{producteurId}/dettes")
  public List<Achat> listerDettesParProducteur(@PathVariable Long producteurId) {
    return achatService.listerDettesParProducteur(producteurId);
  }

  @GetMapping("/total-dettes")
  public BigDecimal totalDettes() {
    return achatService.totalDettes();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Achat creer(@Valid @RequestBody AchatRequest requete) {
    return achatService.creerAchat(requete);
  }

  @PostMapping("/{id}/paiement")
  public Achat payer(@PathVariable Long id, @Valid @RequestBody PaiementRequest requete) {
    return achatService.payerAchat(id, requete);
  }
}
