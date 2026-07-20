package com.example.demo.service;

import com.example.demo.dto.StatistiquesResponse;
import com.example.demo.entity.Achat;
import com.example.demo.entity.Depense;
import com.example.demo.entity.Vente;
import com.example.demo.repository.AchatRepository;
import com.example.demo.repository.DepenseRepository;
import com.example.demo.repository.VenteRepository;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Calcule les séries mensuelles utilisées pour les graphiques du tableau de bord : évolution des
 * ventes, des achats, des dépenses et du bénéfice mensuel.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatistiqueService {
  private static final DateTimeFormatter FORMAT_MOIS = DateTimeFormatter.ofPattern("yyyy-MM");

  private final VenteRepository venteRepository;
  private final AchatRepository achatRepository;
  private final DepenseRepository depenseRepository;

  public StatistiquesResponse construire() {
    Map<String, BigDecimal> ventesParMois = grouperVentesParMois();
    Map<String, BigDecimal> achatsParMois = grouperAchatsParMois();
    Map<String, BigDecimal> depensesParMois = grouperDepensesParMois();
    Map<String, BigDecimal> beneficeParMois =
        calculerBeneficeParMois(ventesParMois, achatsParMois, depensesParMois);

    Map<String, BigDecimal> depensesParCategorie = new LinkedHashMap<>();
    for (Object[] ligne : depenseRepository.calculerTotalParCategorie()) {
      depensesParCategorie.put(ligne[0].toString(), (BigDecimal) ligne[1]);
    }

    return StatistiquesResponse.builder()
        .ventesParMois(ventesParMois)
        .achatsParMois(achatsParMois)
        .depensesParMois(depensesParMois)
        .beneficeParMois(beneficeParMois)
        .depensesParCategorie(depensesParCategorie)
        .build();
  }

  private Map<String, BigDecimal> grouperVentesParMois() {
    Map<String, BigDecimal> resultat = new TreeMap<>();
    for (Vente vente : venteRepository.findAll()) {
      String mois = vente.getDateVente().format(FORMAT_MOIS);
      resultat.merge(mois, vente.getMontantTotal(), BigDecimal::add);
    }
    return resultat;
  }

  private Map<String, BigDecimal> grouperAchatsParMois() {
    Map<String, BigDecimal> resultat = new TreeMap<>();
    for (Achat achat : achatRepository.findAll()) {
      String mois = achat.getDateAchat().format(FORMAT_MOIS);
      resultat.merge(mois, achat.getMontantTotal(), BigDecimal::add);
    }
    return resultat;
  }

  private Map<String, BigDecimal> grouperDepensesParMois() {
    Map<String, BigDecimal> resultat = new TreeMap<>();
    for (Depense depense : depenseRepository.findAll()) {
      String mois = depense.getDate().format(FORMAT_MOIS);
      resultat.merge(mois, depense.getMontant(), BigDecimal::add);
    }
    return resultat;
  }

  private Map<String, BigDecimal> calculerBeneficeParMois(
      Map<String, BigDecimal> ventes,
      Map<String, BigDecimal> achats,
      Map<String, BigDecimal> depenses) {
    Map<String, BigDecimal> resultat = new TreeMap<>();
    for (String mois : ventes.keySet()) {
      BigDecimal v = ventes.getOrDefault(mois, BigDecimal.ZERO);
      BigDecimal a = achats.getOrDefault(mois, BigDecimal.ZERO);
      BigDecimal d = depenses.getOrDefault(mois, BigDecimal.ZERO);
      resultat.put(mois, v.subtract(a).subtract(d));
    }
    return resultat;
  }
}
