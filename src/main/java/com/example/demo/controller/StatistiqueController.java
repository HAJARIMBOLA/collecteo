package com.example.demo.controller;

import com.example.demo.dto.StatistiquesResponse;
import com.example.demo.service.StatistiqueService;
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
