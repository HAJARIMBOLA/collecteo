package com.example.demo.controller;

import com.example.demo.dto.BanqueTransactionRequest;
import com.example.demo.dto.CompteBancaireRequest;
import com.example.demo.entity.BanqueTransaction;
import com.example.demo.entity.CompteBancaire;
import com.example.demo.service.BanqueService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/banque")
@RequiredArgsConstructor
public class BanqueController {
  private final BanqueService banqueService;

  @GetMapping("/comptes")
  public List<CompteBancaire> listerComptes() {
    return banqueService.listerComptes();
  }

  @GetMapping("/comptes/{id}")
  public CompteBancaire trouverCompte(@PathVariable Long id) {
    return banqueService.trouverCompteParId(id);
  }

  @GetMapping("/solde-total")
  public BigDecimal soldeTotal() {
    return banqueService.soldeTotalBanques();
  }

  @PostMapping("/comptes")
  @ResponseStatus(HttpStatus.CREATED)
  public CompteBancaire creerCompte(@Valid @RequestBody CompteBancaireRequest requete) {
    return banqueService.creerCompte(requete);
  }

  @GetMapping("/comptes/{id}/transactions")
  public List<BanqueTransaction> listerTransactions(@PathVariable Long id) {
    return banqueService.listerTransactions(id);
  }

  @PostMapping("/transactions")
  @ResponseStatus(HttpStatus.CREATED)
  public BanqueTransaction enregistrerTransaction(
      @Valid @RequestBody BanqueTransactionRequest requete) {
    return banqueService.enregistrerTransaction(requete);
  }
}
