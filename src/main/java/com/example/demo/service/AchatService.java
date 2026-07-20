package com.example.demo.service;

import com.example.demo.dto.AchatRequest;
import com.example.demo.dto.PaiementRequest;
import com.example.demo.entity.Achat;
import com.example.demo.entity.Producteur;
import com.example.demo.entity.Produit;
import com.example.demo.entity.enums.CategorieTransactionCaisse;
import com.example.demo.entity.enums.StatutPaiement;
import com.example.demo.entity.enums.TypeTransactionCaisse;
import com.example.demo.exception.BusinessException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.AchatRepository;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gère le cycle de vie complet d'un achat : 1. Création -> augmente le stock du produit, calcule le
 * statut de paiement, enregistre une SORTIE de caisse si un paiement comptant est fait. 2. Paiement
 * complémentaire -> réduit le montant restant (la dette envers le producteur), enregistre une
 * nouvelle SORTIE de caisse.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AchatService {
  private final AchatRepository achatRepository;
  private final ProducteurService producteurService;
  private final ProduitService produitService;
  private final CaisseService caisseService;

  public List<Achat> listerTous() {
    return achatRepository.findAll();
  }

  public Achat trouverParId(Long id) {
    return achatRepository
        .findByIdAvecRelations(id)
        .orElseThrow(() -> new ResourceNotFoundException("Achat introuvable avec l'id " + id));
  }

  public List<Achat> listerParProducteur(Long producteurId) {
    return achatRepository.findByProducteurIdOrderByDateAchatDesc(producteurId);
  }

  public List<Achat> listerDettesParProducteur(Long producteurId) {
    return achatRepository.trouverDettesParProducteur(producteurId);
  }

  public BigDecimal totalDettes() {
    return achatRepository.calculerTotalDettes();
  }

  public BigDecimal totalAchats() {
    return achatRepository.calculerTotalAchats();
  }

  public Achat creerAchat(AchatRequest requete) {
    Producteur producteur = producteurService.trouverParId(requete.getProducteurId());
    Produit produit = produitService.trouverParId(requete.getProduitId());

    BigDecimal montantTotal = requete.getQuantite().multiply(requete.getPrixUnitaire());
    BigDecimal montantPaye =
        requete.getMontantPaye() == null ? BigDecimal.ZERO : requete.getMontantPaye();

    if (montantPaye.compareTo(montantTotal) > 0) {
      throw new BusinessException(
          "Le montant payé ne peut pas dépasser le montant total de l'achat");
    }

    BigDecimal montantRestant = montantTotal.subtract(montantPaye);

    Achat achat =
        Achat.builder()
            .producteur(producteur)
            .produit(produit)
            .quantite(requete.getQuantite())
            .prixUnitaire(requete.getPrixUnitaire())
            .montantTotal(montantTotal)
            .montantPaye(montantPaye)
            .montantRestant(montantRestant)
            .statut(determinerStatut(montantPaye, montantTotal))
            .build();

    Achat achatEnregistre = achatRepository.save(achat);

    // Entrée en stock + recalcul du prix moyen d'achat
    produitService.enregistrerEntreeStock(
        produit, requete.getQuantite(), requete.getPrixUnitaire());

    // Trace en caisse si paiement comptant
    if (montantPaye.compareTo(BigDecimal.ZERO) > 0) {
      caisseService.enregistrerMouvementAutomatique(
          TypeTransactionCaisse.SORTIE,
          CategorieTransactionCaisse.ACHAT,
          montantPaye,
          "Paiement achat #" + achatEnregistre.getId() + " - " + produit.getNom(),
          achatEnregistre.getId());
    }

    return achatEnregistre;
  }

  /** Enregistre un paiement complémentaire sur un achat existant (réduction de la dette). */
  public Achat payerAchat(Long achatId, PaiementRequest requete) {
    Achat achat = trouverParId(achatId);

    if (requete.getMontant().compareTo(achat.getMontantRestant()) > 0) {
      throw new BusinessException(
          "Le montant payé dépasse le montant restant dû (" + achat.getMontantRestant() + ")");
    }

    achat.setMontantPaye(achat.getMontantPaye().add(requete.getMontant()));
    achat.setMontantRestant(achat.getMontantRestant().subtract(requete.getMontant()));
    achat.setStatut(determinerStatut(achat.getMontantPaye(), achat.getMontantTotal()));

    Achat achatMisAJour = achatRepository.save(achat);

    caisseService.enregistrerMouvementAutomatique(
        TypeTransactionCaisse.SORTIE,
        CategorieTransactionCaisse.ACHAT,
        requete.getMontant(),
        "Paiement complémentaire achat #" + achat.getId(),
        achat.getId());

    return achatMisAJour;
  }

  private StatutPaiement determinerStatut(BigDecimal montantPaye, BigDecimal montantTotal) {
    if (montantPaye.compareTo(BigDecimal.ZERO) == 0) return StatutPaiement.IMPAYEE;
    if (montantPaye.compareTo(montantTotal) >= 0) return StatutPaiement.PAYEE;
    return StatutPaiement.PARTIELLEMENT_PAYEE;
  }
}
