package hei.school.add.service;

import hei.school.add.dto.DashboardResponse;
import hei.school.add.repository.AchatRepository;
import hei.school.add.repository.CaisseTransactionRepository;
import hei.school.add.repository.ClientRepository;
import hei.school.add.repository.CompteBancaireRepository;
import hei.school.add.repository.DepenseRepository;
import hei.school.add.repository.DetteRepository;
import hei.school.add.repository.PretBancaireRepository;
import hei.school.add.repository.ProducteurRepository;
import hei.school.add.repository.ProduitRepository;
import hei.school.add.repository.VenteRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Agrège les données financières de TOUS les modules pour le tableau de bord / module "Finance" :
 * ventes, achats, dépenses, stock, caisse, banque, créances, dettes (producteurs + fournisseurs) et
 * prêts bancaires.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {
  private final VenteRepository venteRepository;
  private final AchatRepository achatRepository;
  private final ProduitRepository produitRepository;
  private final CaisseTransactionRepository caisseTransactionRepository;
  private final ProducteurRepository producteurRepository;
  private final ClientRepository clientRepository;
  private final CompteBancaireRepository compteBancaireRepository;
  private final DepenseRepository depenseRepository;
  private final DetteRepository detteRepository;
  private final PretBancaireRepository pretBancaireRepository;

  public DashboardResponse construire() {
    BigDecimal chiffreAffaires = venteRepository.calculerTotalVentes();
    BigDecimal totalAchats = achatRepository.calculerTotalAchats();
    BigDecimal totalDepenses = depenseRepository.calculerTotalDepenses();
    BigDecimal beneficeBrut = chiffreAffaires.subtract(totalAchats);
    BigDecimal beneficeNet = beneficeBrut.subtract(totalDepenses);

    BigDecimal soldeCaisse = caisseTransactionRepository.calculerSoldeCaisse();
    BigDecimal soldeBanques = compteBancaireRepository.calculerSoldeTotalBanques();

    BigDecimal dettesProducteurs = achatRepository.calculerTotalDettes();
    BigDecimal dettesFournisseurs = detteRepository.calculerTotalDettesFournisseurs();

    return DashboardResponse.builder()
        .chiffreAffairesVentes(chiffreAffaires)
        .totalAchats(totalAchats)
        .totalDepenses(totalDepenses)
        .beneficeBrut(beneficeBrut)
        .beneficeNet(beneficeNet)
        .valeurStock(produitRepository.calculerValeurTotaleStock())
        .soldeCaisse(soldeCaisse)
        .soldeBanques(soldeBanques)
        .tresorerieTotale(soldeCaisse.add(soldeBanques))
        .totalCreances(venteRepository.calculerTotalCreances())
        .totalDettesProducteurs(dettesProducteurs)
        .totalDettesFournisseurs(dettesFournisseurs)
        .totalDettes(dettesProducteurs.add(dettesFournisseurs))
        .totalEmprunte(pretBancaireRepository.calculerTotalEmprunte())
        .totalRembourse(pretBancaireRepository.calculerTotalRembourse())
        .capitalRestantPrets(pretBancaireRepository.calculerCapitalRestantTotal())
        .nombreProduits(produitRepository.count())
        .nombreProducteurs(producteurRepository.count())
        .nombreClients(clientRepository.count())
        .build();
  }
}
