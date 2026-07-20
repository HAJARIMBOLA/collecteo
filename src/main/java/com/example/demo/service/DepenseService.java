package com.example.demo.service;

import com.example.demo.dto.DepenseRequest;
import com.example.demo.entity.Depense;
import com.example.demo.entity.enums.CategorieDepense;
import com.example.demo.entity.enums.CategorieTransactionCaisse;
import com.example.demo.entity.enums.TypeTransactionCaisse;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.DepenseRepository;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gère les dépenses de l'entreprise. Chaque dépense crée automatiquement une SORTIE de caisse, pour
 * que la caisse et la comptabilité restent synchronisées.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class DepenseService {
  private final DepenseRepository depenseRepository;
  private final CaisseService caisseService;

  public List<Depense> listerToutes() {
    return depenseRepository.findAllByOrderByDateDesc();
  }

  public Depense trouverParId(Long id) {
    return depenseRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Dépense introuvable avec l'id " + id));
  }

  public List<Depense> listerParCategorie(CategorieDepense categorie) {
    return depenseRepository.findByCategorieOrderByDateDesc(categorie);
  }

  public BigDecimal totalDepenses() {
    return depenseRepository.calculerTotalDepenses();
  }

  public Map<String, BigDecimal> totalParCategorie() {
    Map<String, BigDecimal> resultat = new HashMap<>();
    for (Object[] ligne : depenseRepository.calculerTotalParCategorie()) {
      resultat.put(ligne[0].toString(), (BigDecimal) ligne[1]);
    }
    return resultat;
  }

  public Depense creer(DepenseRequest requete) {
    Depense depense =
        Depense.builder()
            .categorie(requete.getCategorie())
            .montant(requete.getMontant())
            .description(requete.getDescription())
            .build();

    Depense depenseEnregistree = depenseRepository.save(depense);

    caisseService.enregistrerMouvementAutomatique(
        TypeTransactionCaisse.SORTIE,
        CategorieTransactionCaisse.DIVERS,
        requete.getMontant(),
        "Dépense "
            + requete.getCategorie()
            + (requete.getDescription() != null ? " - " + requete.getDescription() : ""),
        depenseEnregistree.getId());

    return depenseEnregistree;
  }
}
