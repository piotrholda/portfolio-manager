package piotrholda.portfoliomanager.quotation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import piotrholda.portfoliomanager.Ticker;
import piotrholda.portfoliomanager.strategy.Quotation;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Deprecated
@Disabled("Api disabled by Yahoo.")
class YahooQuotationSourceTest {

    private YahooQuotationSource yahooQuotationSource;

    @BeforeEach
    void setUp() {
        yahooQuotationSource = new YahooQuotationSource();
    }

    @Test
    void shouldFetchRealTimeQuotation()  {
        // given
        String symbol = "AAPL"; // Apple Inc.
        Ticker ticker = new Ticker(symbol, "NASDAQ", "USD");

        // when
        List<Quotation> quotations = yahooQuotationSource.getQuotations(ticker);

        // then
        Quotation quotation = quotations.stream()
                .filter(q -> q.getDate().equals(LocalDate.of(2025,8,22)))
                .findFirst()
                .orElse(null);
        assertNotNull(quotation);
        assertNotNull(quotation.getClosePrice());
        assertEquals(227.76d, quotation.getClosePrice());
    }
}