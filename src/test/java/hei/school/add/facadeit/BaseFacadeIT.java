package hei.school.add.facadeit;

import com.fasterxml.jackson.databind.ObjectMapper;
import hei.school.add.dto.AuthRequest;
import hei.school.add.dto.AuthResponse;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;

/**
 * Classe de base pour tous les tests *FacadeIT.
 *
 * <p>"FacadeIT" = test d'intégration de bout en bout : le test joue le rôle d'un vrai client HTTP
 * (TestRestTemplate) et traverse TOUTE la pile applicative - Controller -> Service -> Repository ->
 * Base de données H2 réelle (en mémoire) - exactement comme le ferait Postman ou le frontend. Aucun
 * mock : c'est le contraire des tests *Test (unitaires, avec Mockito).
 *
 * <p>Le profil "test" (application-test.yml) bascule sur H2 en mémoire pour ne jamais toucher à la
 * vraie base Neon/PostgreSQL pendant les tests.
 *
 * <p>webEnvironment = RANDOM_PORT démarre une vraie instance de l'application sur un port
 * aléatoire, comme en production.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(BaseFacadeIT.ClientHttpConfig.class)
public abstract class BaseFacadeIT {
  @LocalServerPort protected int port;

  @Autowired protected TestRestTemplate restTemplate;

  @Autowired protected ObjectMapper objectMapper;

  protected String token;

  /**
   * Remplace le client HTTP par défaut de TestRestTemplate (basé sur HttpURLConnection du JDK) par
   * Apache HttpClient5. Le client JDK plante avec une HttpRetryException ("cannot retry due to
   * server authentication, in streaming mode") dès qu'une requête POST avec corps reçoit une
   * réponse 401 - exactement le cas du test de login avec mauvais mot de passe.
   */
  @TestConfiguration
  static class ClientHttpConfig {
    @Bean
    RestTemplateBuilder restTemplateBuilder() {
      return new RestTemplateBuilder()
          .requestFactory(() -> new HttpComponentsClientHttpRequestFactory());
    }
  }

  protected String baseUrl() {
    return "http://localhost:" + port + "/api";
  }

  /**
   * Se connecte avec le compte admin créé automatiquement au démarrage par DataInitializer (admin /
   * admin123) et récupère un token JWT valide, réutilisé pour tous les appels authentifiés du test.
   */
  @BeforeEach
  void seConnecter() {
    AuthRequest requete = new AuthRequest();
    requete.setNomUtilisateur("admin");
    requete.setMotDePasse("admin123");

    ResponseEntity<AuthResponse> reponse =
        restTemplate.postForEntity(baseUrl() + "/auth/login", requete, AuthResponse.class);

    this.token = reponse.getBody().getToken();
  }

  /** En-têtes JSON + Authorization: Bearer <token>, pour tous les appels protégés. */
  protected HttpHeaders enTetesAuthentifiees() {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
    return headers;
  }

  protected <T> ResponseEntity<T> postAuthentifie(
      String chemin, Object corps, Class<T> typeReponse) {
    HttpEntity<Object> requete = new HttpEntity<>(corps, enTetesAuthentifiees());
    return restTemplate.exchange(baseUrl() + chemin, HttpMethod.POST, requete, typeReponse);
  }

  protected <T> ResponseEntity<T> getAuthentifie(String chemin, Class<T> typeReponse) {
    HttpEntity<Void> requete = new HttpEntity<>(enTetesAuthentifiees());
    return restTemplate.exchange(baseUrl() + chemin, HttpMethod.GET, requete, typeReponse);
  }
}
