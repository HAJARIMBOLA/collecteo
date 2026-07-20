package com.example.demo.service;

import com.example.demo.entity.Produit;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.ProduitRepository;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProduitService {
  private final ProduitRepository produitRepository;

  public List<Produit> listerTous() {
    return produitRepository.findAll();
  }

  public Produit trouverParId(Long id) {
    return produitRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable avec l'id " + id));
  }

  public Produit creer(Produit produit) {
    return produitRepository.save(produit);
  }

  public Produit modifier(Long id, Produit donnees) {
    Produit existant = trouverParId(id);
    existant.setNom(donnees.getNom());
    existant.setCategorie(donnees.getCategorie());
    existant.setUnite(donnees.getUnite());
    return produitRepository.save(existant);
  }

  public void supprimer(Long id) {
    Produit existant = trouverParId(id);
    produitRepository.delete(existant);
  }

  public BigDecimal valeurTotaleStock() {
    return produitRepository.calculerValeurTotaleStock();
  }

  public List<Produit> produitsEnRupture(BigDecimal seuil) {
    return produitRepository.trouverProduitsEnRupture(seuil);
  }

  /**
   * Augmente le stock après un achat et recalcule le prix moyen d'achat pondéré par les quantités
   * (moyenne pondérée mobile).
   */
  public void enregistrerEntreeStock(
      Produit produit, BigDecimal quantiteAchetee, BigDecimal prixUnitaireAchat) {
    BigDecimal stockActuel = produit.getQuantiteStock();
    BigDecimal valeurStockActuelle = stockActuel.multiply(produit.getPrixMoyenAchat());
    BigDecimal valeurNouvelleEntree = quantiteAchetee.multiply(prixUnitaireAchat);

    BigDecimal nouveauStock = stockActuel.add(quantiteAchetee);
    BigDecimal nouveauPrixMoyen =
        nouveauStock.compareTo(BigDecimal.ZERO) > 0
            ? valeurStockActuelle
                .add(valeurNouvelleEntree)
                .divide(nouveauStock, 2, java.math.RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

    produit.setQuantiteStock(nouveauStock);
    produit.setPrixMoyenAchat(nouveauPrixMoyen);
    produitRepository.save(produit);
  }

  /** Diminue le stock après une vente et met à jour le dernier prix moyen de vente. */
  public void enregistrerSortieStock(
      Produit produit, BigDecimal quantiteVendue, BigDecimal prixUnitaireVente) {
    produit.setQuantiteStock(produit.getQuantiteStock().subtract(quantiteVendue));
    produit.setPrixMoyenVente(prixUnitaireVente);
    produitRepository.save(produit);
  }
}
