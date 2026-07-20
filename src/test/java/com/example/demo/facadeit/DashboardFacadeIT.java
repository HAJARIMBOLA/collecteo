package com.example.demo.facadeit;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.dto.*;
import com.example.demo.entity.*;
import com.example.demo.entity.enums.CategorieDepense;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/**
 * Vérifie que le Dashboard agrège correctement les données de TOUS les modules (achats, ventes,
 * dépenses) à travers la pile complète.
 *
 * <p>Important : comme Spring réutilise le même contexte applicatif (donc la même base H2) entre
 * les classes *FacadeIT lors d'un même run, ce test compare un ÉTAT AVANT / ÉTAT APRÈS (delta)
 * plutôt que des valeurs absolues, pour rester fiable quel que soit l'ordre d'exécution des autres
 * tests d'intégration.
 */
class DashboardFacadeIT extends BaseFacadeIT {
  @Test
  void dashboard_doitAgregerVentesAchatsEtDepenses() {
    DashboardResponse avant = getAuthentifie("/dashboard", DashboardResponse.class).getBody();

    Producteur producteur =
        postAuthentifie(
                "/producteurs",
                Producteur.builder().nom("Producteur Dashboard").build(),
                Producteur.class)
            .getBody();
    Client client =
        postAuthentifie("/clients", Client.builder().nom("Client Dashboard").build(), Client.class)
            .getBody();
    Produit produit =
        postAuthentifie(
                "/produits",
                Produit.builder().nom("Girofle").categorie("Épice").unite("kg").build(),
                Produit.class)
            .getBody();

    // Achat : 1 000 000 Ar (payé comptant intégralement, pour ne pas polluer les dettes)
    AchatRequest achatRequete = new AchatRequest();
    achatRequete.setProducteurId(producteur.getId());
    achatRequete.setProduitId(produit.getId());
    achatRequete.setQuantite(BigDecimal.valueOf(100));
    achatRequete.setPrixUnitaire(BigDecimal.valueOf(10000));
    achatRequete.setMontantPaye(BigDecimal.valueOf(1_000_000));
    postAuthentifie("/achats", achatRequete, Achat.class);

    // Vente : 600 000 Ar (50 kg à 12 000, payée comptant intégralement)
    VenteRequest venteRequete = new VenteRequest();
    venteRequete.setClientId(client.getId());
    venteRequete.setProduitId(produit.getId());
    venteRequete.setQuantite(BigDecimal.valueOf(50));
    venteRequete.setPrixUnitaire(BigDecimal.valueOf(12000));
    venteRequete.setMontantPaye(BigDecimal.valueOf(600_000));
    postAuthentifie("/ventes", venteRequete, Vente.class);

    // Dépense : 50 000 Ar
    DepenseRequest depenseRequete = new DepenseRequest();
    depenseRequete.setCategorie(CategorieDepense.CARBURANT);
    depenseRequete.setMontant(BigDecimal.valueOf(50_000));
    postAuthentifie("/depenses", depenseRequete, Depense.class);

    DashboardResponse apres = getAuthentifie("/dashboard", DashboardResponse.class).getBody();

    assertThat(apres.getChiffreAffairesVentes().subtract(avant.getChiffreAffairesVentes()))
        .isEqualByComparingTo("600000");
    assertThat(apres.getTotalAchats().subtract(avant.getTotalAchats()))
        .isEqualByComparingTo("1000000");
    assertThat(apres.getTotalDepenses().subtract(avant.getTotalDepenses()))
        .isEqualByComparingTo("50000");

    // Delta bénéfice brut = 600 000 (ventes) - 1 000 000 (achats) = -400 000
    BigDecimal deltaBeneficeBrut = apres.getBeneficeBrut().subtract(avant.getBeneficeBrut());
    assertThat(deltaBeneficeBrut).isEqualByComparingTo("-400000");

    // Delta bénéfice net = -400 000 - 50 000 (dépense) = -450 000
    BigDecimal deltaBeneficeNet = apres.getBeneficeNet().subtract(avant.getBeneficeNet());
    assertThat(deltaBeneficeNet).isEqualByComparingTo("-450000");

    // Le nombre de produits doit avoir augmenté d'exactement 1 (celui créé dans ce test)
    assertThat(apres.getNombreProduits() - avant.getNombreProduits()).isEqualTo(1);
  }
}
