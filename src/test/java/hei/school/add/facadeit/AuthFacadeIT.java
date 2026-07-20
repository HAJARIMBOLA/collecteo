package hei.school.add.facadeit;

import static org.assertj.core.api.Assertions.assertThat;

import hei.school.add.dto.AuthRequest;
import hei.school.add.dto.AuthResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Vérifie le flux d'authentification de bout en bout et la protection des routes : - login avec de
 * bons identifiants -> 200 + token JWT - login avec de mauvais identifiants -> 401 - appel d'une
 * route protégée sans token -> 401/403 - appel d'une route protégée avec token -> 200
 */
class AuthFacadeIT extends BaseFacadeIT {
  @Test
  void login_avecBonsIdentifiants_doitRenvoyerUnToken() {
    AuthRequest requete = new AuthRequest();
    requete.setNomUtilisateur("admin");
    requete.setMotDePasse("admin123");

    ResponseEntity<AuthResponse> reponse =
        restTemplate.postForEntity(baseUrl() + "/auth/login", requete, AuthResponse.class);

    assertThat(reponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(reponse.getBody()).isNotNull();
    assertThat(reponse.getBody().getToken()).isNotBlank();
    assertThat(reponse.getBody().getNomUtilisateur()).isEqualTo("admin");
  }

  @Test
  void login_avecMauvaisMotDePasse_doitRenvoyer401() {
    AuthRequest requete = new AuthRequest();
    requete.setNomUtilisateur("admin");
    requete.setMotDePasse("mauvais-mot-de-passe");

    ResponseEntity<String> reponse =
        restTemplate.postForEntity(baseUrl() + "/auth/login", requete, String.class);

    assertThat(reponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void appelRouteProtegee_sansToken_doitEtreRefuse() {
    ResponseEntity<String> reponse =
        restTemplate.getForEntity(baseUrl() + "/producteurs", String.class);

    assertThat(reponse.getStatusCode().value()).isIn(401, 403);
  }

  @Test
  void appelRouteProtegee_avecToken_doitReussir() {
    ResponseEntity<Object[]> reponse = getAuthentifie("/producteurs", Object[].class);

    assertThat(reponse.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void routeUtilisateurs_reserveeAdministrateur_estAccessibleAvecCompteAdmin() {
    ResponseEntity<Object[]> reponse = getAuthentifie("/utilisateurs", Object[].class);

    assertThat(reponse.getStatusCode()).isEqualTo(HttpStatus.OK);
  }
}
