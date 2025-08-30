package piotrholda.portfoliomanager.strategy;

import lombok.Data;
import piotrholda.portfoliomanager.Ticker;

import java.math.BigDecimal;
import java.time.LocalDate;

import static piotrholda.portfoliomanager.infrastructure.Math.MATH_CONTEXT;

@Data
public class Quotation implements Comparable<Quotation> {
    private Ticker ticker;
    private LocalDate date;
    private BigDecimal closePrice;
    private BigDecimal adjustedClosePrice;


    BigDecimal profitPercent(Quotation previousQuotation) {
        if (previousQuotation == null || previousQuotation.getClosePrice() == null || this.getClosePrice() == null) {
            throw new IllegalArgumentException("Next quotation or close prices cannot be null.");
        }
        return getClosePrice()
                .subtract(previousQuotation.getClosePrice())
                .divide(previousQuotation.getClosePrice(), MATH_CONTEXT)
                .multiply(BigDecimal.valueOf(100));
    }

    @Override
    public int compareTo(Quotation o) {
        return date.compareTo(o.getDate());
    }
}
