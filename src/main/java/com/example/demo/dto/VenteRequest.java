package com.example.demo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VenteRequest {
  @NotNull private Long clientId;

  @NotNull private Long produitId;

  @NotNull
  @DecimalMin(value = "0.01", message = "La quantité doit être positive")
  private BigDecimal quantite;

  @NotNull
  @DecimalMin(value = "0.01", message = "Le prix unitaire doit être positif")
  private BigDecimal prixUnitaire;

  /** Montant payé immédiatement (comptant). 0 ou non renseigné = vente entièrement à crédit. */
  @DecimalMin(value = "0.0", message = "Le montant payé ne peut pas être négatif")
  private BigDecimal montantPaye = BigDecimal.ZERO;
}
