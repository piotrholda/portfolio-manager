package piotrholda.portfoliomanager.strategy;

import lombok.Data;
import piotrholda.portfoliomanager.Ticker;

import java.time.LocalDate;

@Data
public class Quotation implements Comparable<Quotation> {
    private Ticker ticker;
    private LocalDate date;
    private Double closePrice;
    private Double adjustedClosePrice;

    double profitPercent(Quotation previousQuotation) {
        if (previousQuotation == null || previousQuotation.getClosePrice() == null || this.getClosePrice() == null) {
            throw new IllegalArgumentException("Next quotation or close prices cannot be null.");
        }
        return ((getClosePrice() - previousQuotation.getClosePrice()) / previousQuotation.getClosePrice()) * 100;
    }

    @Override
    public int compareTo(Quotation o) {
        return date.compareTo(o.getDate());
    }
}
