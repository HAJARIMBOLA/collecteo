package hei.school.add.service;

import hei.school.add.dto.BanqueTransactionRequest;
import hei.school.add.dto.CompteBancaireRequest;
import hei.school.add.entity.BanqueTransaction;
import hei.school.add.entity.CompteBancaire;
import hei.school.add.entity.enums.TypeTransactionCaisse;
import hei.school.add.exception.BusinessException;
import hei.school.add.exception.ResourceNotFoundException;
import hei.school.add.repository.BanqueTransactionRepository;
import hei.school.add.repository.CompteBancaireRepository;
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

  /** Enregistre un dépôt ou un retrait et met à jour le solde du compte en conséquence. */
  public BanqueTransaction enregistrerTransaction(BanqueTransactionRequest requete) {
    CompteBancaire compte = trouverCompteParId(requete.getCompteBancaireId());

    if (requete.getType() == TypeTransactionCaisse.SORTIE
        && compte.getSolde().compareTo(requete.getMontant()) < 0) {
      throw new BusinessException("Solde insuffisant sur le compte " + compte.getNomBanque());
    }

    BigDecimal nouveauSolde =
        requete.getType() == TypeTransactionCaisse.ENTREE
            ? compte.getSolde().add(requete.getMontant())
            : compte.getSolde().subtract(requete.getMontant());
    compte.setSolde(nouveauSolde);
    compteBancaireRepository.save(compte);

    BanqueTransaction transaction =
        BanqueTransaction.builder()
            .compteBancaire(compte)
            .type(requete.getType())
            .montant(requete.getMontant())
            .description(requete.getDescription())
            .build();
    return banqueTransactionRepository.save(transaction);
  }

  /** Utilisé en interne par PretBancaireService pour créditer/débiter un compte automatiquement. */
  void appliquerMouvement(
      CompteBancaire compte, TypeTransactionCaisse type, BigDecimal montant, String description) {
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
    banqueTransactionRepository.save(transaction);
  }
}
