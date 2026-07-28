package com.example.demo.endpoint.event.model;

import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Demande d'envoi par email d'un relevé de compte (caisse + situation financière).
 *
 * <p>Produit par l'endpoint POST /api/rapports/releve, consommé de façon asynchrone par {@link
 * com.example.demo.service.event.ReleveCompteDemandeService} qui génère le PDF puis l'envoie en
 * pièce jointe.
 *
 * <p>Emplacement imposé par Poja : les classes d'événement doivent être dans le package
 * {@code your.package.name.endpoint.event.model}.
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Data
@EqualsAndHashCode(callSuper = false)
@ToString
public class ReleveCompteDemande extends PojaEvent {

  /** Adresse à laquelle envoyer le relevé. */
  private String emailDestinataire;

  /** Nom de l'utilisateur qui a demandé le relevé, affiché dans le corps du mail. */
  private String demandePar;

  /**
   * Temps max estimé du traitement (requêtes DB + génération PDF + envoi SES). Doit rester
   * inférieur au timeout du worker (600 s).
   */
  @Override
  public Duration maxConsumerDuration() {
    return Duration.ofMinutes(3);
  }

  /** Délai avant nouvelle tentative en cas d'échec (ex. SES temporairement indisponible). */
  @Override
  public Duration maxConsumerBackoffBetweenRetries() {
    return Duration.ofSeconds(30);
  }
}