package hei.school.add.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Vue d'ensemble financière de l'entreprise ("module Finance") : caisse, banque, stock, créances,
 * dettes (producteurs + fournisseurs), prêts bancaires, dépenses, et bénéfice net réel tenant
 * compte de toutes ces composantes.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DashboardResponse {
  private BigDecimal chiffreAffairesVentes;
  private BigDecimal totalAchats;
  private BigDecimal totalDepenses;
  private BigDecimal beneficeBrut; // chiffreAffairesVentes - totalAchats
  private BigDecimal beneficeNet; // beneficeBrut - totalDepenses

  private BigDecimal valeurStock;

  private BigDecimal soldeCaisse;
  private BigDecimal soldeBanques;
  private BigDecimal tresorerieTotale; // soldeCaisse + soldeBanques

  private BigDecimal totalCreances; // argent que les clients doivent
  private BigDecimal totalDettesProducteurs; // argent dû aux producteurs (achats à crédit)
  private BigDecimal totalDettesFournisseurs; // autres dettes fournisseurs/partenaires
  private BigDecimal totalDettes; // somme des deux ci-dessus

  private BigDecimal totalEmprunte;
  private BigDecimal totalRembourse;
  private BigDecimal capitalRestantPrets;

  private long nombreProduits;
  private long nombreProducteurs;
  private long nombreClients;
}
