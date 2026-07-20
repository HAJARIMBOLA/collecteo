package hei.school.add.facadeit;

import static org.assertj.core.api.Assertions.assertThat;

import hei.school.add.dto.CompteBancaireRequest;
import hei.school.add.dto.PretBancaireRequest;
import hei.school.add.entity.CompteBancaire;
import hei.school.add.entity.PretBancaire;
import hei.school.add.entity.enums.StatutPret;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Vérifie le flux complet d'un prêt bancaire : déblocage (crédite le compte), puis remboursement
 * (débite le compte et réduit le capital restant).
 */
class PretBancaireFacadeIT extends BaseFacadeIT {
  @Test
  void creerPretPuisRembourser_doitMettreAJourLeCompteEtLeCapitalRestant() {
    CompteBancaireRequest compteRequete = new CompteBancaireRequest();
    compteRequete.setNomBanque("BNI");
    compteRequete.setNumeroCompte("00112233");
    compteRequete.setSoldeInitial(BigDecimal.valueOf(200_000));

    CompteBancaire compte =
        postAuthentifie("/banque/comptes", compteRequete, CompteBancaire.class).getBody();

    PretBancaireRequest pretRequete = new PretBancaireRequest();
    pretRequete.setCompteBancaireId(compte.getId());
    pretRequete.setMontantEmprunte(BigDecimal.valueOf(1_000_000));
    pretRequete.setDatePret(LocalDate.now());
    pretRequete.setDureeMois(12);
    pretRequete.setTauxInteret(BigDecimal.valueOf(10));

    ResponseEntity<PretBancaire> reponsePret =
        postAuthentifie("/prets", pretRequete, PretBancaire.class);
    assertThat(reponsePret.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    PretBancaire pret = reponsePret.getBody();
    assertThat(pret.getStatut()).isEqualTo(StatutPret.EN_COURS);
    assertThat(pret.getMensualite()).isGreaterThan(BigDecimal.ZERO);

    // Le déblocage du prêt doit avoir crédité le compte bancaire
    ResponseEntity<CompteBancaire> reponseCompteApresDeblocage =
        getAuthentifie("/banque/comptes/" + compte.getId(), CompteBancaire.class);
    assertThat(reponseCompteApresDeblocage.getBody().getSolde())
        .isEqualByComparingTo("1200000"); // 200 000 + 1 000 000

    // Premier remboursement
    String url =
        UriComponentsBuilder.fromUriString("/prets/" + pret.getId() + "/remboursement")
            .queryParam("montant", "100000")
            .toUriString();
    ResponseEntity<PretBancaire> reponseRemboursement =
        postAuthentifie(url, null, PretBancaire.class);

    assertThat(reponseRemboursement.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(reponseRemboursement.getBody().getCapitalRestant()).isEqualByComparingTo("900000");

    // Le remboursement doit avoir débité le compte bancaire
    ResponseEntity<CompteBancaire> reponseCompteApresRemboursement =
        getAuthentifie("/banque/comptes/" + compte.getId(), CompteBancaire.class);
    assertThat(reponseCompteApresRemboursement.getBody().getSolde())
        .isEqualByComparingTo("1100000"); // 1 200 000 - 100 000
  }
}
