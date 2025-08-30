package piotrholda.portfoliomanager.corporateaction;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
class Split implements CorporateAction {
    private LocalDate date;
    private BigDecimal ratio;
}
