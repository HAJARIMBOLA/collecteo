package hei.school.add.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PretBancaireRequest {
  @NotNull private Long compteBancaireId;

  @NotNull
  @DecimalMin(value = "0.01", message = "Le montant emprunté doit être positif")
  private BigDecimal montantEmprunte;

  @NotNull private LocalDate datePret;

  @NotNull private Integer dureeMois;

  /** Taux d'intérêt annuel en pourcentage, ex : 12.5 */
  @NotNull
  @DecimalMin(value = "0.0", message = "Le taux d'intérêt ne peut pas être négatif")
  private BigDecimal tauxInteret;
}
