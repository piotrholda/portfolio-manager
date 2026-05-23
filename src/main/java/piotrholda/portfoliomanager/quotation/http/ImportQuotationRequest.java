package piotrholda.portfoliomanager.quotation.http;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import piotrholda.portfoliomanager.Ticker;

@Data
class ImportQuotationRequest {
    private String code;
    private String exchangeCode = "NYSE";
    private String currencyCode;

    Ticker toTicker() {
        return Ticker.builder()
                .code(code)
                .exchangeCode(exchangeCode)
                .currencyCode(resolveCurrencyCode())
                .build();
    }

    private String resolveCurrencyCode() {
        if (StringUtils.isNotBlank(currencyCode)) {
            return currencyCode;
        }
        if ("WSE".equalsIgnoreCase(exchangeCode)) {
            return "PLN";
        }
        return "USD";
    }
}
