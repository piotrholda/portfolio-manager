package piotrholda.portfoliomanager.strategy;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

class QuotationTest {

    @Test
    void shouldCalculateProfit() {
        // Given
        Quotation previousQuotation = new Quotation();
        previousQuotation.setClosePrice(BigDecimal.valueOf(100.00));
        Quotation quotation = new Quotation();
        quotation.setClosePrice(BigDecimal.valueOf(120.11));

        // When
        BigDecimal profit = quotation.profitPercent(previousQuotation);

        // Then
        assertEquals("20.11", profit.setScale(2, RoundingMode.HALF_UP).toString(), "Profit should be 20.11");
    }

}