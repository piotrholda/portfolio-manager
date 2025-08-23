package piotrholda.portfoliomanager.strategy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QuotationTest {

    @Test
    void shouldCalculateProfit() {
        // Given
        Quotation previousQuotation = new Quotation();
        previousQuotation.setClosePrice(100.00);
        Quotation quotation = new Quotation();
        quotation.setClosePrice(120.11);

        // When
        double profit = quotation.profitPercent(previousQuotation);

        // Then
        assertEquals(20.11, profit, "Profit should be 20.11");
    }

}