package com.example.demo.controller;

import com.example.demo.dto.CaisseTransactionRequest;
import com.example.demo.entity.CaisseTransaction;
import com.example.demo.service.CaisseService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/caisse")
@RequiredArgsConstructor
public class CaisseController {
  private final CaisseService caisseService;

  @GetMapping("/transactions")
  public List<CaisseTransaction> listerTransactions() {
    return caisseService.listerTransactions();
  }

  @GetMapping("/solde")
  public BigDecimal solde() {
    return caisseService.soldeActuel();
  }

  /**
   * Saisie manuelle d'un mouvement de caisse : apport propriétaire, salaire, carburant, transport,
   * divers.
   */
  @PostMapping("/transactions")
  @ResponseStatus(HttpStatus.CREATED)
  public CaisseTransaction enregistrer(@Valid @RequestBody CaisseTransactionRequest requete) {
    return caisseService.enregistrerMouvement(
        requete.getType(), requete.getCategorie(), requete.getMontant(), requete.getDescription());
  }
}
