package com.example.demo.dto;

import com.example.demo.entity.enums.CategorieDepense;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DepenseRequest {
  @NotNull private CategorieDepense categorie;

  @NotNull
  @DecimalMin(value = "0.01", message = "Le montant doit être positif")
  private BigDecimal montant;

  private String description;
}
