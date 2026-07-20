package com.example.demo.dto;

import java.math.BigDecimal;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Évolution mensuelle des indicateurs clés, utilisable pour tracer des graphiques côté frontend.
 * Chaque map est indexée par mois au format "yyyy-MM" (ex: "2026-06").
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StatistiquesResponse {
  private Map<String, BigDecimal> ventesParMois;
  private Map<String, BigDecimal> achatsParMois;
  private Map<String, BigDecimal> depensesParMois;
  private Map<String, BigDecimal> beneficeParMois;
  private Map<String, BigDecimal> depensesParCategorie;
}
