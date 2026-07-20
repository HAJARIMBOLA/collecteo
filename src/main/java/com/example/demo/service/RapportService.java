package com.example.demo.service;

import com.example.demo.dto.DashboardResponse;
import com.example.demo.entity.Achat;
import com.example.demo.entity.Vente;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RapportService {
  private final AchatService achatService;
  private final VenteService venteService;
  private final DashboardService dashboardService;

  private static final DateTimeFormatter FORMAT_DATE =
      DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

  // ---------- EXCEL ----------

  public byte[] genererExcelVentes() throws IOException {
    List<Vente> ventes = venteService.listerToutes();
    try (XSSFWorkbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      Sheet sheet = workbook.createSheet("Ventes");
      CellStyle styleEntete = creerStyleEntete(workbook);

      Row entete = sheet.createRow(0);
      String[] colonnes = {
        "ID",
        "Client",
        "Produit",
        "Quantité",
        "Prix unitaire",
        "Montant total",
        "Montant payé",
        "Montant restant",
        "Statut",
        "Date"
      };
      for (int i = 0; i < colonnes.length; i++) {
        Cell cell = entete.createCell(i);
        cell.setCellValue(colonnes[i]);
        cell.setCellStyle(styleEntete);
      }

      int ligne = 1;
      for (Vente v : ventes) {
        Row row = sheet.createRow(ligne++);
        row.createCell(0).setCellValue(v.getId());
        row.createCell(1).setCellValue(v.getClient().getNom());
        row.createCell(2).setCellValue(v.getProduit().getNom());
        row.createCell(3).setCellValue(v.getQuantite().doubleValue());
        row.createCell(4).setCellValue(v.getPrixUnitaire().doubleValue());
        row.createCell(5).setCellValue(v.getMontantTotal().doubleValue());
        row.createCell(6).setCellValue(v.getMontantPaye().doubleValue());
        row.createCell(7).setCellValue(v.getMontantRestant().doubleValue());
        row.createCell(8).setCellValue(v.getStatut().name());
        row.createCell(9).setCellValue(v.getDateVente().format(FORMAT_DATE));
      }

      for (int i = 0; i < colonnes.length; i++) sheet.autoSizeColumn(i);

      workbook.write(out);
      return out.toByteArray();
    }
  }

  public byte[] genererExcelAchats() throws IOException {
    List<Achat> achats = achatService.listerTous();
    try (XSSFWorkbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      Sheet sheet = workbook.createSheet("Achats");
      CellStyle styleEntete = creerStyleEntete(workbook);

      Row entete = sheet.createRow(0);
      String[] colonnes = {
        "ID",
        "Producteur",
        "Produit",
        "Quantité",
        "Prix unitaire",
        "Montant total",
        "Montant payé",
        "Montant restant",
        "Statut",
        "Date"
      };
      for (int i = 0; i < colonnes.length; i++) {
        Cell cell = entete.createCell(i);
        cell.setCellValue(colonnes[i]);
        cell.setCellStyle(styleEntete);
      }

      int ligne = 1;
      for (Achat a : achats) {
        Row row = sheet.createRow(ligne++);
        row.createCell(0).setCellValue(a.getId());
        row.createCell(1).setCellValue(a.getProducteur().getNom());
        row.createCell(2).setCellValue(a.getProduit().getNom());
        row.createCell(3).setCellValue(a.getQuantite().doubleValue());
        row.createCell(4).setCellValue(a.getPrixUnitaire().doubleValue());
        row.createCell(5).setCellValue(a.getMontantTotal().doubleValue());
        row.createCell(6).setCellValue(a.getMontantPaye().doubleValue());
        row.createCell(7).setCellValue(a.getMontantRestant().doubleValue());
        row.createCell(8).setCellValue(a.getStatut().name());
        row.createCell(9).setCellValue(a.getDateAchat().format(FORMAT_DATE));
      }

      for (int i = 0; i < colonnes.length; i++) sheet.autoSizeColumn(i);

      workbook.write(out);
      return out.toByteArray();
    }
  }

  private CellStyle creerStyleEntete(XSSFWorkbook workbook) {
    CellStyle style = workbook.createCellStyle();
    org.apache.poi.ss.usermodel.Font font = workbook.createFont();
    font.setBold(true);
    style.setFont(font);
    return style;
  }

  // ---------- PDF ----------

  /** Rapport financier global au format PDF, basé sur le tableau de bord (module Finance). */
  public byte[] genererPdfRapportFinancier() throws IOException {
    DashboardResponse dashboard = dashboardService.construire();

    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      Document document = new Document(PageSize.A4);
      PdfWriter.getInstance(document, out);
      document.open();

      Font titreFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
      Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);
      Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11);

      Paragraph titre = new Paragraph("Collecteo - Rapport financier", titreFont);
      titre.setAlignment(Element.ALIGN_CENTER);
      document.add(titre);
      document.add(new Paragraph(" "));

      ajouterSection(document, "Chiffre d'affaires et bénéfice", sectionFont);
      ajouterLigneTableau(
          document,
          normalFont,
          new String[] {
            "Chiffre d'affaires (ventes)", formater(dashboard.getChiffreAffairesVentes())
          },
          new String[] {"Total achats", formater(dashboard.getTotalAchats())},
          new String[] {"Total dépenses", formater(dashboard.getTotalDepenses())},
          new String[] {"Bénéfice brut", formater(dashboard.getBeneficeBrut())},
          new String[] {"Bénéfice net", formater(dashboard.getBeneficeNet())});

      ajouterSection(document, "Trésorerie", sectionFont);
      ajouterLigneTableau(
          document,
          normalFont,
          new String[] {"Solde caisse", formater(dashboard.getSoldeCaisse())},
          new String[] {"Solde banques", formater(dashboard.getSoldeBanques())},
          new String[] {"Trésorerie totale", formater(dashboard.getTresorerieTotale())},
          new String[] {"Valeur du stock", formater(dashboard.getValeurStock())});

      ajouterSection(document, "Créances et dettes", sectionFont);
      ajouterLigneTableau(
          document,
          normalFont,
          new String[] {"Total créances (clients)", formater(dashboard.getTotalCreances())},
          new String[] {
            "Dettes envers producteurs", formater(dashboard.getTotalDettesProducteurs())
          },
          new String[] {"Dettes fournisseurs", formater(dashboard.getTotalDettesFournisseurs())},
          new String[] {"Total dettes", formater(dashboard.getTotalDettes())});

      ajouterSection(document, "Prêts bancaires", sectionFont);
      ajouterLigneTableau(
          document,
          normalFont,
          new String[] {"Total emprunté", formater(dashboard.getTotalEmprunte())},
          new String[] {"Total remboursé", formater(dashboard.getTotalRembourse())},
          new String[] {"Capital restant dû", formater(dashboard.getCapitalRestantPrets())});

      document.close();
      return out.toByteArray();
    } catch (com.lowagie.text.DocumentException e) {
      throw new IOException("Erreur lors de la génération du PDF", e);
    }
  }

  private void ajouterSection(Document document, String titre, Font font)
      throws com.lowagie.text.DocumentException {
    Paragraph p = new Paragraph(titre, font);
    p.setSpacingBefore(10);
    p.setSpacingAfter(5);
    document.add(p);
  }

  private void ajouterLigneTableau(Document document, Font font, String[]... lignes)
      throws com.lowagie.text.DocumentException {
    PdfPTable table = new PdfPTable(2);
    table.setWidthPercentage(100);
    for (String[] ligne : lignes) {
      PdfPCell c1 = new PdfPCell(new Paragraph(ligne[0], font));
      PdfPCell c2 = new PdfPCell(new Paragraph(ligne[1], font));
      c1.setBorder(0);
      c2.setBorder(0);
      table.addCell(c1);
      table.addCell(c2);
    }
    document.add(table);
  }

  private String formater(java.math.BigDecimal valeur) {
    return valeur == null ? "0" : valeur.toPlainString() + " Ar";
  }
}
