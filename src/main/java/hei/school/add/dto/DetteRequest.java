package hei.school.add.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DetteRequest {
  @NotBlank private String fournisseur;

  private String motif;

  @NotNull
  @DecimalMin(value = "0.01", message = "Le montant doit être positif")
  private BigDecimal montant;

  private LocalDate dateLimite;
}
