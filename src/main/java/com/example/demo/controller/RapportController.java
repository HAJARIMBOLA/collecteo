package com.example.demo.controller;

import com.example.demo.endpoint.event.EventProducer;
import com.example.demo.endpoint.event.model.ReleveCompteDemande;
import com.example.demo.service.RapportService;
import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Export des rapports en Excel (.xlsx) et PDF, et envoi asynchrone du relevé de compte. */
@RestController
@RequestMapping("/api/rapports")
@RequiredArgsConstructor
public class RapportController {
  private final RapportService rapportService;
  private final EventProducer<ReleveCompteDemande> releveEventProducer;

  @GetMapping("/ventes/excel")
  public ResponseEntity<byte[]> exporterVentesExcel() throws IOException {
    byte[] fichier = rapportService.genererExcelVentes();
    return reponseFichier(
        fichier,
        "ventes.xlsx",
        MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
  }

  @GetMapping("/achats/excel")
  public ResponseEntity<byte[]> exporterAchatsExcel() throws IOException {
    byte[] fichier = rapportService.genererExcelAchats();
    return reponseFichier(
        fichier,
        "achats.xlsx",
        MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
  }

  @GetMapping("/financier/pdf")
  public ResponseEntity<byte[]> exporterRapportFinancierPdf() throws IOException {
    byte[] fichier = rapportService.genererPdfRapportFinancier();
    return reponseFichier(fichier, "rapport-financier.pdf", MediaType.APPLICATION_PDF);
  }

  /**
   * Demande l'envoi du relevé de compte par email.
   *
   * <p>Répond immédiatement en 202 Accepted : la génération du PDF et l'envoi du mail sont délégués
   * à un worker asynchrone, ce qui évite de bloquer l'appelant et de heurter le timeout de 30 s de
   * la Lambda frontale.
   */
  @PostMapping("/releve")
  public ResponseEntity<Map<String, String>> demanderReleve(
      @RequestParam String email, Principal principal) {
    var evenement =
        ReleveCompteDemande.builder()
            .emailDestinataire(email)
            .demandePar(principal == null ? null : principal.getName())
            .build();

    releveEventProducer.accept(List.of(evenement));

    return ResponseEntity.accepted()
        .body(
            Map.of(
                "message", "Le relevé vous sera envoyé à " + email + " dans quelques instants."));
  }

  private ResponseEntity<byte[]> reponseFichier(byte[] contenu, String nomFichier, MediaType type) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentDisposition(ContentDisposition.attachment().filename(nomFichier).build());
    return ResponseEntity.ok().headers(headers).contentType(type).body(contenu);
  }
}
