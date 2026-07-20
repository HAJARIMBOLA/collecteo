package com.example.demo.dto;

import com.example.demo.entity.enums.CategorieTransactionCaisse;
import com.example.demo.entity.enums.TypeTransactionCaisse;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/** Utilisé pour les mouvements de caisse saisis manuellement (apport, salaire, carburant...). */
@Getter
@Setter
public class CaisseTransactionRequest {
  @NotNull private TypeTransactionCaisse type;

  @NotNull private CategorieTransactionCaisse categorie;

  @NotNull
  @DecimalMin(value = "0.01", message = "Le montant doit être positif")
  private BigDecimal montant;

  private String description;
}
