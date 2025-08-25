package piotrholda.portfoliomanager.strategy.http;

import lombok.Data;
import piotrholda.portfoliomanager.Ticker;

@Data
class StrategyRequestTicker {
    private String code;
    private String exchangeCode;
    private String currencyCode;

    Ticker toTicker() {
        return new Ticker(code, exchangeCode, currencyCode);
    }
}
