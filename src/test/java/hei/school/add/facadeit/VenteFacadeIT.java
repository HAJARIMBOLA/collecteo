package hei.school.add.facadeit;

import static org.assertj.core.api.Assertions.assertThat;

import hei.school.add.dto.AchatRequest;
import hei.school.add.dto.VenteRequest;
import hei.school.add.entity.Achat;
import hei.school.add.entity.Client;
import hei.school.add.entity.Producteur;
import hei.school.add.entity.Produit;
import hei.school.add.entity.Vente;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Vérifie le flux complet d'une vente, y compris le refus d'une vente quand le stock réel en base
 * est insuffisant (la règle métier la plus critique de l'application).
 */
class VenteFacadeIT extends BaseFacadeIT {
  @Test
  void creerVente_avecStockSuffisant_doitDiminuerLeStockEnBase() {
    Producteur producteur = creerProducteur();
    Client client = creerClient();
    Produit produit = creerProduit();

    approvisionnerStock(producteur, produit, BigDecimal.valueOf(100), BigDecimal.valueOf(2000));

    VenteRequest requeteVente = new VenteRequest();
    requeteVente.setClientId(client.getId());
    requeteVente.setProduitId(produit.getId());
    requeteVente.setQuantite(BigDecimal.valueOf(30));
    requeteVente.setPrixUnitaire(BigDecimal.valueOf(3500));
    requeteVente.setMontantPaye(BigDecimal.valueOf(105000));

    ResponseEntity<Vente> reponse = postAuthentifie("/ventes", requeteVente, Vente.class);

    assertThat(reponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(reponse.getBody().getMontantTotal()).isEqualByComparingTo("105000");

    ResponseEntity<Produit> reponseProduit =
        getAuthentifie("/produits/" + produit.getId(), Produit.class);
    assertThat(reponseProduit.getBody().getQuantiteStock()).isEqualByComparingTo("70"); // 100 - 30
  }

  @Test
  void creerVente_avecStockInsuffisant_doitEchouerEtNePasToucherLeStock() {
    Producteur producteur = creerProducteur();
    Client client = creerClient();
    Produit produit = creerProduit();

    approvisionnerStock(producteur, produit, BigDecimal.valueOf(10), BigDecimal.valueOf(2000));

    VenteRequest requeteVente = new VenteRequest();
    requeteVente.setClientId(client.getId());
    requeteVente.setProduitId(produit.getId());
    requeteVente.setQuantite(BigDecimal.valueOf(500)); // largement supérieur au stock (10)
    requeteVente.setPrixUnitaire(BigDecimal.valueOf(3500));

    ResponseEntity<Map> reponse = postAuthentifie("/ventes", requeteVente, Map.class);

    assertThat(reponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(reponse.getBody().get("message").toString()).contains("Stock insuffisant");

    // Le stock en base ne doit pas avoir bougé
    ResponseEntity<Produit> reponseProduit =
        getAuthentifie("/produits/" + produit.getId(), Produit.class);
    assertThat(reponseProduit.getBody().getQuantiteStock()).isEqualByComparingTo("10");
  }

  private void approvisionnerStock(
      Producteur producteur, Produit produit, BigDecimal quantite, BigDecimal prixUnitaire) {
    AchatRequest achat = new AchatRequest();
    achat.setProducteurId(producteur.getId());
    achat.setProduitId(produit.getId());
    achat.setQuantite(quantite);
    achat.setPrixUnitaire(prixUnitaire);
    achat.setMontantPaye(quantite.multiply(prixUnitaire));
    ResponseEntity<Achat> reponse = postAuthentifie("/achats", achat, Achat.class);
    assertThat(reponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
  }

  private Producteur creerProducteur() {
    Producteur producteur =
        Producteur.builder().nom("Producteur Stock").telephone("0341111111").build();
    return postAuthentifie("/producteurs", producteur, Producteur.class).getBody();
  }

  private Client creerClient() {
    Client client = Client.builder().nom("Client Stock").telephone("0342222222").build();
    return postAuthentifie("/clients", client, Client.class).getBody();
  }

  private Produit creerProduit() {
    Produit produit = Produit.builder().nom("Riz").categorie("Céréale").unite("kg").build();
    return postAuthentifie("/produits", produit, Produit.class).getBody();
  }
}
