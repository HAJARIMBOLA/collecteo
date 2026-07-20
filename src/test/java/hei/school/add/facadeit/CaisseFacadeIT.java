package hei.school.add.facadeit;

import static org.assertj.core.api.Assertions.assertThat;

import hei.school.add.dto.CaisseTransactionRequest;
import hei.school.add.entity.CaisseTransaction;
import hei.school.add.entity.enums.CategorieTransactionCaisse;
import hei.school.add.entity.enums.TypeTransactionCaisse;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Vérifie les mouvements manuels de caisse (apport, salaire...) et le calcul du solde. Comme
 * d'autres tests (achats/ventes payés comptant) génèrent aussi des écritures de caisse
 * automatiques, on compare un delta avant/après plutôt qu'un solde absolu.
 */
class CaisseFacadeIT extends BaseFacadeIT {
  @Test
  void enregistrerApportPuisSortie_doitFaireEvoluerLeSoldeDuMontantAttendu() {
    BigDecimal soldeAvant = getAuthentifie("/caisse/solde", BigDecimal.class).getBody();

    CaisseTransactionRequest apport = new CaisseTransactionRequest();
    apport.setType(TypeTransactionCaisse.ENTREE);
    apport.setCategorie(CategorieTransactionCaisse.APPORT_PROPRIETAIRE);
    apport.setMontant(BigDecimal.valueOf(1_000_000));
    apport.setDescription("Apport initial du propriétaire");

    ResponseEntity<CaisseTransaction> reponseApport =
        postAuthentifie("/caisse/transactions", apport, CaisseTransaction.class);
    assertThat(reponseApport.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    CaisseTransactionRequest salaire = new CaisseTransactionRequest();
    salaire.setType(TypeTransactionCaisse.SORTIE);
    salaire.setCategorie(CategorieTransactionCaisse.SALAIRE);
    salaire.setMontant(BigDecimal.valueOf(300_000));
    salaire.setDescription("Salaire du mois");

    postAuthentifie("/caisse/transactions", salaire, CaisseTransaction.class);

    BigDecimal soldeApres = getAuthentifie("/caisse/solde", BigDecimal.class).getBody();

    // +1 000 000 (apport) - 300 000 (salaire) = +700 000, quel que soit le solde de départ
    assertThat(soldeApres.subtract(soldeAvant)).isEqualByComparingTo("700000");
  }
}
