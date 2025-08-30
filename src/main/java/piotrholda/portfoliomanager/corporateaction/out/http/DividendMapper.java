package piotrholda.portfoliomanager.corporateaction.out.http;

import piotrholda.portfoliomanager.Ticker;
import piotrholda.portfoliomanager.corporateaction.Dividend;

import java.math.BigDecimal;
import java.time.LocalDate;

class DividendMapper {
    static Dividend toDomain(Ticker ticker, piotrholda.portfoliomanager.stockapi.client.model.Dividend dividend) {
        Dividend result = new Dividend();
        result.setTicker(ticker);
        result.setExDividendDate(LocalDate.parse(dividend.getExDividendDate()));
        result.setPayableDate(LocalDate.parse(dividend.getPayableDate()));
        String dividendAmountChange = dividend.getDividendAmountChange();
        String[] strings = dividendAmountChange.trim().split("\\s+");
        result.setAmount(new BigDecimal(strings[0].trim()));
        result.setCurrency(strings[1].trim());
        return result;
    }
}
