package com.example.demo.service;

import com.example.demo.dto.DashboardResponse;
import com.example.demo.repository.AchatRepository;
import com.example.demo.repository.CaisseTransactionRepository;
import com.example.demo.repository.ClientRepository;
import com.example.demo.repository.CompteBancaireRepository;
import com.example.demo.repository.DepenseRepository;
import com.example.demo.repository.DetteRepository;
import com.example.demo.repository.PretBancaireRepository;
import com.example.demo.repository.ProducteurRepository;
import com.example.demo.repository.ProduitRepository;
import com.example.demo.repository.VenteRepository;
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
