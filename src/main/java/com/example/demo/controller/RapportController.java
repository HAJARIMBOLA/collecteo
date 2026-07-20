package com.example.demo.controller;

import com.example.demo.service.RapportService;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Export des rapports en Excel (.xlsx) et PDF. */
@RestController
@RequestMapping("/api/rapports")
@RequiredArgsConstructor
public class RapportController {
  private final RapportService rapportService;

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

  private ResponseEntity<byte[]> reponseFichier(byte[] contenu, String nomFichier, MediaType type) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentDisposition(ContentDisposition.attachment().filename(nomFichier).build());
    return ResponseEntity.ok().headers(headers).contentType(type).body(contenu);
  }
}
