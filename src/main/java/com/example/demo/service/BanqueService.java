package com.example.demo.service;

import com.example.demo.dto.BanqueTransactionRequest;
import com.example.demo.dto.CompteBancaireRequest;
import com.example.demo.entity.BanqueTransaction;
import com.example.demo.entity.CompteBancaire;
import com.example.demo.entity.enums.TypeTransactionCaisse;
import com.example.demo.exception.BusinessException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.BanqueTransactionRepository;
import com.example.demo.repository.CompteBancaireRepository;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gère les comptes bancaires et leurs mouvements (dépôts, retraits, virements). Chaque transaction
 * met à jour le solde du compte concerné.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class BanqueService {
  private final CompteBancaireRepository compteBancaireRepository;
  private final BanqueTransactionRepository banqueTransactionRepository;

  public List<CompteBancaire> listerComptes() {
    return compteBancaireRepository.findAll();
  }

  public CompteBancaire trouverCompteParId(Long id) {
    return compteBancaireRepository
        .findById(id)
        .orElseThrow(
            () -> new ResourceNotFoundException("Compte bancaire introuvable avec l'id " + id));
  }

  public CompteBancaire creerCompte(CompteBancaireRequest requete) {
    CompteBancaire compte =
        CompteBancaire.builder()
            .nomBanque(requete.getNomBanque())
            .numeroCompte(requete.getNumeroCompte())
            .solde(requete.getSoldeInitial() == null ? BigDecimal.ZERO : requete.getSoldeInitial())
            .build();
    return compteBancaireRepository.save(compte);
  }

  public BigDecimal soldeTotalBanques() {
    return compteBancaireRepository.calculerSoldeTotalBanques();
  }

  public List<BanqueTransaction> listerTransactions(Long compteBancaireId) {
    return banqueTransactionRepository.findByCompteBancaireIdOrderByDateDesc(compteBancaireId);
  }

  /** Enregistre un dépôt ou un retrait initié directement par l'utilisateur via l'API. */
  public BanqueTransaction enregistrerTransaction(BanqueTransactionRequest requete) {
    CompteBancaire compte = trouverCompteParId(requete.getCompteBancaireId());
    return appliquerMouvement(
        compte, requete.getType(), requete.getMontant(), requete.getDescription());
  }

  /**
   * Crédite ou débite un compte et enregistre le mouvement correspondant. Utilisé aussi bien pour
   * les dépôts/retraits manuels (via l'API) que pour les mouvements automatiques déclenchés par
   * PretBancaireService (décaissement/remboursement de prêt). Refuse toujours une SORTIE si le
   * solde du compte est insuffisant, quel que soit l'appelant.
   */
  BanqueTransaction appliquerMouvement(
      CompteBancaire compte, TypeTransactionCaisse type, BigDecimal montant, String description) {
    if (type == TypeTransactionCaisse.SORTIE && compte.getSolde().compareTo(montant) < 0) {
      throw new BusinessException("Solde insuffisant sur le compte " + compte.getNomBanque());
    }

    BigDecimal nouveauSolde =
        type == TypeTransactionCaisse.ENTREE
            ? compte.getSolde().add(montant)
            : compte.getSolde().subtract(montant);
    compte.setSolde(nouveauSolde);
    compteBancaireRepository.save(compte);

    BanqueTransaction transaction =
        BanqueTransaction.builder()
            .compteBancaire(compte)
            .type(type)
            .montant(montant)
            .description(description)
            .build();
    return banqueTransactionRepository.save(transaction);
  }
}
