package com.example.demo.service;

import com.example.demo.dto.PaiementRequest;
import com.example.demo.dto.VenteRequest;
import com.example.demo.entity.Client;
import com.example.demo.entity.Produit;
import com.example.demo.entity.Vente;
import com.example.demo.entity.enums.CategorieTransactionCaisse;
import com.example.demo.entity.enums.StatutPaiement;
import com.example.demo.entity.enums.TypeTransactionCaisse;
import com.example.demo.exception.BusinessException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.VenteRepository;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gère le cycle de vie complet d'une vente : 1. Création -> vérifie le stock disponible, diminue le
 * stock, calcule le statut de paiement, enregistre une ENTREE de caisse si un paiement comptant est
 * fait. 2. Paiement complémentaire -> réduit le montant restant (la créance sur le client),
 * enregistre une nouvelle ENTREE de caisse.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class VenteService {
  private final VenteRepository venteRepository;
  private final ClientService clientService;
  private final ProduitService produitService;
  private final CaisseService caisseService;

  public List<Vente> listerToutes() {
    return venteRepository.findAll();
  }

  public Vente trouverParId(Long id) {
    return venteRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Vente introuvable avec l'id " + id));
  }

  public List<Vente> listerParClient(Long clientId) {
    return venteRepository.findByClientIdOrderByDateVenteDesc(clientId);
  }

  public List<Vente> listerCreancesParClient(Long clientId) {
    return venteRepository.trouverCreancesParClient(clientId);
  }

  public BigDecimal totalCreances() {
    return venteRepository.calculerTotalCreances();
  }

  public BigDecimal totalVentes() {
    return venteRepository.calculerTotalVentes();
  }

  public Vente creerVente(VenteRequest requete) {
    Client client = clientService.trouverParId(requete.getClientId());
    Produit produit = produitService.trouverParId(requete.getProduitId());

    if (produit.getQuantiteStock().compareTo(requete.getQuantite()) < 0) {
      throw new BusinessException(
          "Stock insuffisant pour "
              + produit.getNom()
              + " (disponible : "
              + produit.getQuantiteStock()
              + " "
              + produit.getUnite()
              + ")");
    }

    BigDecimal montantTotal = requete.getQuantite().multiply(requete.getPrixUnitaire());
    BigDecimal montantPaye =
        requete.getMontantPaye() == null ? BigDecimal.ZERO : requete.getMontantPaye();

    if (montantPaye.compareTo(montantTotal) > 0) {
      throw new BusinessException(
          "Le montant payé ne peut pas dépasser le montant total de la vente");
    }

    BigDecimal montantRestant = montantTotal.subtract(montantPaye);

    Vente vente =
        Vente.builder()
            .client(client)
            .produit(produit)
            .quantite(requete.getQuantite())
            .prixUnitaire(requete.getPrixUnitaire())
            .montantTotal(montantTotal)
            .montantPaye(montantPaye)
            .montantRestant(montantRestant)
            .statut(determinerStatut(montantPaye, montantTotal))
            .build();

    Vente venteEnregistree = venteRepository.save(vente);

    // Sortie de stock
    produitService.enregistrerSortieStock(
        produit, requete.getQuantite(), requete.getPrixUnitaire());

    // Trace en caisse si paiement comptant
    if (montantPaye.compareTo(BigDecimal.ZERO) > 0) {
      caisseService.enregistrerMouvementAutomatique(
          TypeTransactionCaisse.ENTREE,
          CategorieTransactionCaisse.VENTE,
          montantPaye,
          "Paiement vente #" + venteEnregistree.getId() + " - " + produit.getNom(),
          venteEnregistree.getId());
    }

    return venteEnregistree;
  }

  /** Enregistre un paiement complémentaire sur une vente existante (réduction de la créance). */
  public Vente payerVente(Long venteId, PaiementRequest requete) {
    Vente vente = trouverParId(venteId);

    if (requete.getMontant().compareTo(vente.getMontantRestant()) > 0) {
      throw new BusinessException(
          "Le montant payé dépasse le montant restant dû (" + vente.getMontantRestant() + ")");
    }

    vente.setMontantPaye(vente.getMontantPaye().add(requete.getMontant()));
    vente.setMontantRestant(vente.getMontantRestant().subtract(requete.getMontant()));
    vente.setStatut(determinerStatut(vente.getMontantPaye(), vente.getMontantTotal()));

    Vente venteMiseAJour = venteRepository.save(vente);

    caisseService.enregistrerMouvementAutomatique(
        TypeTransactionCaisse.ENTREE,
        CategorieTransactionCaisse.REMBOURSEMENT_CLIENT,
        requete.getMontant(),
        "Remboursement créance vente #" + vente.getId(),
        vente.getId());

    return venteMiseAJour;
  }

  private StatutPaiement determinerStatut(BigDecimal montantPaye, BigDecimal montantTotal) {
    if (montantPaye.compareTo(BigDecimal.ZERO) == 0) return StatutPaiement.IMPAYEE;
    if (montantPaye.compareTo(montantTotal) >= 0) return StatutPaiement.PAYEE;
    return StatutPaiement.PARTIELLEMENT_PAYEE;
  }
}
