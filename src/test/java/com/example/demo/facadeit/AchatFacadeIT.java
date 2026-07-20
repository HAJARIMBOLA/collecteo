package com.example.demo.facadeit;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.dto.AchatRequest;
import com.example.demo.dto.PaiementRequest;
import com.example.demo.entity.Achat;
import com.example.demo.entity.Producteur;
import com.example.demo.entity.Produit;
import com.example.demo.entity.enums.StatutPaiement;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Vérifie le flux complet d'un achat à travers TOUTE la pile (HTTP -> Controller -> Service ->
 * Repository -> H2) : création du producteur et du produit, création de l'achat, et vérification
 * que le stock du produit a bien été mis à jour en base.
 */
class AchatFacadeIT extends BaseFacadeIT {
  @Test
  void creerAchat_doitAugmenterLeStockEtCalculerLeRestant() {
    Producteur producteur = creerProducteur("Rakoto Vanille");
    Produit produit = creerProduit("Vanille noire", "kg");

    // Spring réutilise le même contexte (et donc la même base H2) entre les classes
    // de test : on capture le total AVANT pour comparer un delta plutôt qu'une valeur
    // absolue, ce qui rend le test indépendant de l'ordre d'exécution des autres tests.
    BigDecimal dettesAvant = getAuthentifie("/achats/total-dettes", BigDecimal.class).getBody();

    AchatRequest requete = new AchatRequest();
    requete.setProducteurId(producteur.getId());
    requete.setProduitId(produit.getId());
    requete.setQuantite(BigDecimal.valueOf(20));
    requete.setPrixUnitaire(BigDecimal.valueOf(50000));
    requete.setMontantPaye(BigDecimal.valueOf(500000)); // paiement partiel comptant

    ResponseEntity<Achat> reponse = postAuthentifie("/achats", requete, Achat.class);

    assertThat(reponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    Achat achat = reponse.getBody();
    assertThat(achat.getMontantTotal()).isEqualByComparingTo("1000000"); // 20 * 50000
    assertThat(achat.getMontantRestant()).isEqualByComparingTo("500000");
    assertThat(achat.getStatut()).isEqualTo(StatutPaiement.PARTIELLEMENT_PAYEE);

    // Vérifie que le stock du produit a bien été mis à jour EN BASE (pas seulement en mémoire)
    ResponseEntity<Produit> reponseProduit =
        getAuthentifie("/produits/" + produit.getId(), Produit.class);
    assertThat(reponseProduit.getBody().getQuantiteStock()).isEqualByComparingTo("20");
    assertThat(reponseProduit.getBody().getPrixMoyenAchat()).isEqualByComparingTo("50000");

    // Vérifie que la dette envers le producteur a bien augmenté du montant attendu
    BigDecimal dettesApres = getAuthentifie("/achats/total-dettes", BigDecimal.class).getBody();
    assertThat(dettesApres.subtract(dettesAvant)).isEqualByComparingTo("500000");
  }

  @Test
  void payerAchat_doitReduireLeMontantRestant() {
    Producteur producteur = creerProducteur("Rabe Café");
    Produit produit = creerProduit("Café Arabica", "kg");

    AchatRequest requeteAchat = new AchatRequest();
    requeteAchat.setProducteurId(producteur.getId());
    requeteAchat.setProduitId(produit.getId());
    requeteAchat.setQuantite(BigDecimal.valueOf(10));
    requeteAchat.setPrixUnitaire(BigDecimal.valueOf(10000));
    requeteAchat.setMontantPaye(BigDecimal.ZERO); // entièrement à crédit

    Achat achat = postAuthentifie("/achats", requeteAchat, Achat.class).getBody();
    assertThat(achat.getStatut()).isEqualTo(StatutPaiement.IMPAYEE);

    PaiementRequest paiement = new PaiementRequest();
    paiement.setMontant(BigDecimal.valueOf(100000)); // solde tout

    ResponseEntity<Achat> reponsePaiement =
        postAuthentifie("/achats/" + achat.getId() + "/paiement", paiement, Achat.class);

    assertThat(reponsePaiement.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(reponsePaiement.getBody().getMontantRestant()).isEqualByComparingTo("0");
    assertThat(reponsePaiement.getBody().getStatut()).isEqualTo(StatutPaiement.PAYEE);
  }

  private Producteur creerProducteur(String nom) {
    Producteur producteur = Producteur.builder().nom(nom).telephone("0340000000").build();
    return postAuthentifie("/producteurs", producteur, Producteur.class).getBody();
  }

  private Produit creerProduit(String nom, String unite) {
    Produit produit = Produit.builder().nom(nom).categorie("Épice").unite(unite).build();
    return postAuthentifie("/produits", produit, Produit.class).getBody();
  }
}
