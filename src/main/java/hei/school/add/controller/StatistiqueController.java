package hei.school.add.controller;

import hei.school.add.dto.StatistiquesResponse;
import hei.school.add.service.StatistiqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Évolution mensuelle des ventes, achats, dépenses et bénéfice — pour les graphiques du frontend.
 */
@RestController
@RequestMapping("/api/statistiques")
@RequiredArgsConstructor
public class StatistiqueController {
  private final StatistiqueService statistiqueService;

  @GetMapping
  public StatistiquesResponse obtenir() {
    return statistiqueService.construire();
  }
}
