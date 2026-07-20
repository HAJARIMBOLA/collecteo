package hei.school.add.facadeit;

import static org.assertj.core.api.Assertions.assertThat;

import hei.school.add.entity.Producteur;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** CRUD complet de bout en bout pour Producteur, via les vraies routes HTTP. */
class ProducteurFacadeIT extends BaseFacadeIT {
  @Test
  void cycleDeVieComplet_creer_lire_modifier_supprimer() {
    // CREATE
    Producteur nouveau =
        Producteur.builder()
            .nom("Rakoto Jean")
            .telephone("0341234567")
            .adresse("Antsirabe")
            .build();

    ResponseEntity<Producteur> reponseCreation =
        postAuthentifie("/producteurs", nouveau, Producteur.class);
    assertThat(reponseCreation.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    Long id = reponseCreation.getBody().getId();
    assertThat(id).isNotNull();

    // READ
    ResponseEntity<Producteur> reponseLecture =
        getAuthentifie("/producteurs/" + id, Producteur.class);
    assertThat(reponseLecture.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(reponseLecture.getBody().getNom()).isEqualTo("Rakoto Jean");

    // UPDATE
    Producteur modifie =
        Producteur.builder()
            .nom("Rakoto Jean Updated")
            .telephone("0341234567")
            .adresse("Antananarivo")
            .build();
    org.springframework.http.HttpEntity<Producteur> requeteUpdate =
        new org.springframework.http.HttpEntity<>(modifie, enTetesAuthentifiees());
    ResponseEntity<Producteur> reponseUpdate =
        restTemplate.exchange(
            baseUrl() + "/producteurs/" + id,
            org.springframework.http.HttpMethod.PUT,
            requeteUpdate,
            Producteur.class);
    assertThat(reponseUpdate.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(reponseUpdate.getBody().getNom()).isEqualTo("Rakoto Jean Updated");
    assertThat(reponseUpdate.getBody().getAdresse()).isEqualTo("Antananarivo");

    // DELETE
    org.springframework.http.HttpEntity<Void> requeteDelete =
        new org.springframework.http.HttpEntity<>(enTetesAuthentifiees());
    ResponseEntity<Void> reponseDelete =
        restTemplate.exchange(
            baseUrl() + "/producteurs/" + id,
            org.springframework.http.HttpMethod.DELETE,
            requeteDelete,
            Void.class);
    assertThat(reponseDelete.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

    // Vérifie que le producteur a bien disparu
    ResponseEntity<String> reponseApresSuppression =
        restTemplate.exchange(
            baseUrl() + "/producteurs/" + id,
            org.springframework.http.HttpMethod.GET,
            new org.springframework.http.HttpEntity<>(enTetesAuthentifiees()),
            String.class);
    assertThat(reponseApresSuppression.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }
}
