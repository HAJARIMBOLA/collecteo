package com.example.demo.service.event;

import static java.io.File.createTempFile;
import static java.nio.file.Files.write;

import com.example.demo.endpoint.event.model.ReleveCompteDemande;
import com.example.demo.mail.Email;
import com.example.demo.mail.Mailer;
import com.example.demo.service.CaisseService;
import com.example.demo.service.RapportService;
import jakarta.mail.internet.InternetAddress;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Consomme {@link ReleveCompteDemande} : génère le relevé PDF puis l'envoie en pièce jointe.
 *
 * <p>Emplacement et nom imposés par Poja : le consommateur doit être dans
 * {@code your.package.name.service.event} et s'appeler {@code {NomEvenement}Service}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReleveCompteDemandeService implements Consumer<ReleveCompteDemande> {

  private static final DateTimeFormatter FORMAT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

  private final RapportService rapportService;
  private final CaisseService caisseService;
  private final Mailer mailer;

  @Override
  @SneakyThrows
  public void accept(ReleveCompteDemande demande) {
    log.info("Génération du relevé de compte pour {}", demande.getEmailDestinataire());

    var pdf = rapportService.genererPdfRapportFinancier();
    var fichier = ecrireFichierTemporaire(pdf);

    var destinataire = new InternetAddress(demande.getEmailDestinataire());
    var email =
        new Email(
            destinataire,
            List.of(),
            List.of(),
            "Relevé de compte Collecteo — " + LocalDate.now().format(FORMAT_DATE),
            corpsHtml(demande),
            List.of(fichier));

    mailer.accept(email);
    log.info("Relevé envoyé à {}", demande.getEmailDestinataire());
  }

  private File ecrireFichierTemporaire(byte[] contenu) throws Exception {
    var nom = "releve-compte-" + LocalDate.now();
    var fichier = createTempFile(nom, ".pdf");
    write(fichier.toPath(), contenu);
    return fichier;
  }

  private String corpsHtml(ReleveCompteDemande demande) {
    return """
        <p>Bonjour,</p>
        <p>Voici le relevé de compte demandé le %s%s.</p>
        <p>Solde actuel de la caisse : <strong>%s</strong></p>
        <p>Le détail complet se trouve dans le PDF joint.</p>
        <p>— Collecteo</p>
        """
        .formatted(
            LocalDate.now().format(FORMAT_DATE),
            demande.getDemandePar() == null ? "" : " par " + demande.getDemandePar(),
            caisseService.soldeActuel());
  }
}