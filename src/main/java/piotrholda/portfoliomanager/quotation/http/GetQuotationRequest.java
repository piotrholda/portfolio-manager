package piotrholda.portfoliomanager.quotation.http;

import lombok.Data;
import piotrholda.portfoliomanager.Ticker;

@Data
class GetQuotationRequest {
    private String code;
    private String exchangeCode = "NYSE";
    private String currencyCode = "USD";

    Ticker toTicker() {
        return Ticker.builder()
                .code(code)
                .exchangeCode(exchangeCode)
                .currencyCode(currencyCode)
                .build();
    }
}
