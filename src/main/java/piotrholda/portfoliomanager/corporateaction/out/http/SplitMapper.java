package piotrholda.portfoliomanager.corporateaction.out.http;

import piotrholda.portfoliomanager.Ticker;
import piotrholda.portfoliomanager.corporateaction.Split;

import java.math.BigDecimal;
import java.time.LocalDate;

class SplitMapper {
    static Split toDomain(Ticker ticker, piotrholda.portfoliomanager.stockapi.client.model.Split split) {
        Split result = new Split();
        result.setTicker(ticker);
        result.setDate(LocalDate.parse(split.getDate()));
        result.setRatio(new BigDecimal(split.getSplitRatio()));
        return result;
    }
}
