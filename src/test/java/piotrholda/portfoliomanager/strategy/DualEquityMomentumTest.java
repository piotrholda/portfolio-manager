package piotrholda.portfoliomanager.strategy;

import org.junit.jupiter.api.Test;
import piotrholda.portfoliomanager.Ticker;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DualEquityMomentumTest {

    @Test
    void shouldUsePreviousQuotationWhenExactDateIsMissingForInstrument() {
        DualEquityMomentum strategy = new DualEquityMomentum();
        Ticker ticker = Ticker.builder().code("ETFBM40TR").exchangeCode("WSE").currencyCode("PLN").build();

        strategy.getQuotations().put(ticker, List.of(
                new Quotation(ticker, LocalDate.of(1998, 6, 29), BigDecimal.valueOf(10)),
                new Quotation(ticker, LocalDate.of(1998, 7, 1), BigDecimal.valueOf(11))
        ));

        List<LocalDate> dates = List.of(
                LocalDate.of(1998, 6, 29),
                LocalDate.of(1998, 6, 30),
                LocalDate.of(1998, 7, 1)
        );

        Quotation quotation = strategy.findQuotation(LocalDate.of(1998, 6, 30), dates, strategy.getQuotations().get(ticker));

        assertEquals(LocalDate.of(1998, 6, 29), quotation.getDate());
        assertEquals(BigDecimal.valueOf(10), quotation.getClosePrice());
    }
}
