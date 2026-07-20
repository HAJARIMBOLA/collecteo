package hei.school.add.dto;

import hei.school.add.entity.enums.TypeTransactionCaisse;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/** Dépôt, retrait ou virement sur un compte bancaire. */
@Getter
@Setter
public class BanqueTransactionRequest {
  @NotNull private Long compteBancaireId;

  @NotNull private TypeTransactionCaisse type;

  @NotNull
  @DecimalMin(value = "0.01", message = "Le montant doit être positif")
  private BigDecimal montant;

  private String description;
}
