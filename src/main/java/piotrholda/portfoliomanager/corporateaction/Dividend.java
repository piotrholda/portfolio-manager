package piotrholda.portfoliomanager.corporateaction;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
class Dividend implements CorporateAction {
    private LocalDate exDividendDate;
    private LocalDate payableDate;
    private BigDecimal amount;
}
