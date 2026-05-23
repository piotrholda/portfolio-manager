package piotrholda.portfoliomanager.quotation.http;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ImportQuotationRequestTest {

    @Test
    void shouldDefaultCurrencyToPlnForWse() {
        ImportQuotationRequest request = new ImportQuotationRequest();
        request.setCode("ETFBM40TR");
        request.setExchangeCode("WSE");

        var ticker = request.toTicker();

        assertEquals("ETFBM40TR", ticker.getCode());
        assertEquals("WSE", ticker.getExchangeCode());
        assertEquals("PLN", ticker.getCurrencyCode());
    }

    @Test
    void shouldDefaultCurrencyToUsdForNonWse() {
        ImportQuotationRequest request = new ImportQuotationRequest();
        request.setCode("VT");

        var ticker = request.toTicker();

        assertEquals("NYSE", ticker.getExchangeCode());
        assertEquals("USD", ticker.getCurrencyCode());
    }
}
