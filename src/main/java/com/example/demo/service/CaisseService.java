package com.example.demo.service;

import com.example.demo.entity.CaisseTransaction;
import com.example.demo.entity.enums.CategorieTransactionCaisse;
import com.example.demo.entity.enums.TypeTransactionCaisse;
import com.example.demo.repository.CaisseTransactionRepository;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CaisseService {
  private final CaisseTransactionRepository caisseTransactionRepository;

  public List<CaisseTransaction> listerTransactions() {
    return caisseTransactionRepository.findAllByOrderByDateDesc();
  }

  public BigDecimal soldeActuel() {
    return caisseTransactionRepository.calculerSoldeCaisse();
  }

  /** Saisie manuelle : apport du propriétaire, salaire, carburant, transport, divers... */
  public CaisseTransaction enregistrerMouvement(
      TypeTransactionCaisse type,
      CategorieTransactionCaisse categorie,
      BigDecimal montant,
      String description) {
    CaisseTransaction transaction =
        CaisseTransaction.builder()
            .type(type)
            .categorie(categorie)
            .montant(montant)
            .description(description)
            .build();
    return caisseTransactionRepository.save(transaction);
  }

  /**
   * Utilisé en interne par AchatService / VenteService pour tracer automatiquement les paiements.
   */
  public CaisseTransaction enregistrerMouvementAutomatique(
      TypeTransactionCaisse type,
      CategorieTransactionCaisse categorie,
      BigDecimal montant,
      String description,
      Long referenceId) {
    if (montant == null || montant.compareTo(BigDecimal.ZERO) <= 0) {
      return null;
    }
    CaisseTransaction transaction =
        CaisseTransaction.builder()
            .type(type)
            .categorie(categorie)
            .montant(montant)
            .description(description)
            .referenceId(referenceId)
            .build();
    return caisseTransactionRepository.save(transaction);
  }
}
