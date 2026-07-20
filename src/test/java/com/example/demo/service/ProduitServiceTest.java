package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.demo.entity.Produit;
import com.example.demo.repository.ProduitRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests unitaires du calcul du prix moyen pondéré, le point le plus délicat du service Produit
 * (moyenne mobile pondérée par les quantités).
 */
@ExtendWith(MockitoExtension.class)
class ProduitServiceTest {
  @Mock private ProduitRepository produitRepository;

  @InjectMocks private ProduitService produitService;

  @Test
  void enregistrerEntreeStock_premierAchat_prixMoyenEgalAuPrixAchete() {
    Produit produit =
        Produit.builder()
            .id(1L)
            .nom("Vanille")
            .quantiteStock(BigDecimal.ZERO)
            .prixMoyenAchat(BigDecimal.ZERO)
            .build();

    when(produitRepository.save(any(Produit.class))).thenAnswer(i -> i.getArgument(0));

    invoquerEntreeStock(produit, BigDecimal.valueOf(10), BigDecimal.valueOf(2000));

    assertThat(produit.getQuantiteStock()).isEqualByComparingTo("10");
    assertThat(produit.getPrixMoyenAchat()).isEqualByComparingTo("2000");
  }

  @Test
  void enregistrerEntreeStock_deuxiemeAchatPrixDifferent_doitPondererCorrectement() {
    // Stock initial : 10 kg à 2000 Ar -> valeur 20 000
    Produit produit =
        Produit.builder()
            .id(1L)
            .nom("Vanille")
            .quantiteStock(BigDecimal.valueOf(10))
            .prixMoyenAchat(BigDecimal.valueOf(2000))
            .build();

    when(produitRepository.save(any(Produit.class))).thenAnswer(i -> i.getArgument(0));

    // Nouvel achat : 10 kg à 4000 Ar -> valeur 40 000
    invoquerEntreeStock(produit, BigDecimal.valueOf(10), BigDecimal.valueOf(4000));

    // Nouveau stock : 20 kg, valeur totale : 60 000 -> prix moyen attendu : 3000
    assertThat(produit.getQuantiteStock()).isEqualByComparingTo("20");
    assertThat(produit.getPrixMoyenAchat()).isEqualByComparingTo("3000.00");
  }

  @Test
  void enregistrerSortieStock_doitDiminuerLeStockEtMajLeDernierPrixVente() {
    Produit produit =
        Produit.builder()
            .id(1L)
            .nom("Vanille")
            .quantiteStock(BigDecimal.valueOf(20))
            .prixMoyenVente(BigDecimal.ZERO)
            .build();

    when(produitRepository.save(any(Produit.class))).thenAnswer(i -> i.getArgument(0));

    invoquerSortieStock(produit, BigDecimal.valueOf(5), BigDecimal.valueOf(5000));

    assertThat(produit.getQuantiteStock()).isEqualByComparingTo("15");
    assertThat(produit.getPrixMoyenVente()).isEqualByComparingTo("5000");
  }

  // enregistrerEntreeStock / enregistrerSortieStock sont package-private (utilisées par
  // AchatService / VenteService) : on les appelle directement car ce test est dans le même package.
  private void invoquerEntreeStock(Produit produit, BigDecimal quantite, BigDecimal prixUnitaire) {
    produitService.enregistrerEntreeStock(produit, quantite, prixUnitaire);
  }

  private void invoquerSortieStock(Produit produit, BigDecimal quantite, BigDecimal prixUnitaire) {
    produitService.enregistrerSortieStock(produit, quantite, prixUnitaire);
  }
}
