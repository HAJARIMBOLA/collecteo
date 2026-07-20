package hei.school.add.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompteBancaireRequest {
  @NotBlank private String nomBanque;

  private String numeroCompte;

  @DecimalMin(value = "0.0", message = "Le solde initial ne peut pas être négatif")
  private BigDecimal soldeInitial = BigDecimal.ZERO;
}
