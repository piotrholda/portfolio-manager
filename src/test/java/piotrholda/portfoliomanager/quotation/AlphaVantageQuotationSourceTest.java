package piotrholda.portfoliomanager.quotation;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import piotrholda.portfoliomanager.Ticker;

import static org.junit.jupiter.api.Assertions.*;

@Disabled("This test is using real API. Enable only when needed.")
@SpringBootTest
class AlphaVantageQuotationSourceTest {

    @Autowired
    private AlphaVantageQuotationSource quotationSource;

    @Test
    void shouldGetQuotations() {
        // given
        Ticker ticker = new Ticker("IBM", "NYSE", "USD");

        // when
        var quotations = quotationSource.getQuotations(ticker);

        // then
        assertNotNull(quotations);
        assertFalse(quotations.isEmpty());
        assertTrue(quotations.size() > 30);
    }
}