package hei.school.add.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/** Utilisé pour enregistrer un paiement partiel ou total sur un achat ou une vente existant(e). */
@Getter
@Setter
public class PaiementRequest {
  @NotNull
  @DecimalMin(value = "0.01", message = "Le montant payé doit être positif")
  private BigDecimal montant;
}
