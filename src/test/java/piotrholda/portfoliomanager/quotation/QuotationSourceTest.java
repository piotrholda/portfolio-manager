package piotrholda.portfoliomanager.quotation;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import piotrholda.portfoliomanager.Ticker;
import piotrholda.portfoliomanager.strategy.GetQuotations;

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
        Ticker ticker = Ticker.builder().code("VT").exchangeCode("NYSE").currencyCode("USD").build();

        // when
        var quotations = getQuotations.getQuotations(ticker);

        // then
        assertNotNull(quotations);
        assertFalse(quotations.isEmpty());
        assertTrue(quotations.size() > 30);
    }
}

