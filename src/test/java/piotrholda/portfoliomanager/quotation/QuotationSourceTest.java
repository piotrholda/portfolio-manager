package piotrholda.portfoliomanager.quotation;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import piotrholda.portfoliomanager.Ticker;
import piotrholda.portfoliomanager.strategy.GetQuotations;
import piotrholda.portfoliomanager.strategy.Quotation;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class QuotationSourceTest {

    @Autowired
    @Qualifier("getQuotationsUseCase")
    private GetQuotations getQuotations;

    @Disabled("No quotations in database")
    @Test
    void shouldGetQuotations() {
        // given
        Ticker ticker = new Ticker("VT", "NYSE", "USD");
        Quotation quotation = new Quotation();
        quotation.setTicker(ticker);
        quotation.setDate(LocalDate.now());
        quotation.setClosePrice(BigDecimal.valueOf(123.45));

        // when
        var quotations = getQuotations.getQuotations(ticker);

        // then
        assertNotNull(quotations);
        assertFalse(quotations.isEmpty());
        assertTrue(quotations.size() > 30);
    }
}

