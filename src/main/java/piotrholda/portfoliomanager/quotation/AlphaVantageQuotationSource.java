package piotrholda.portfoliomanager.quotation;

import org.springframework.stereotype.Service;
import piotrholda.portfoliomanager.Ticker;
import piotrholda.portfoliomanager.strategy.Quotation;
import piotrholda.portfoliomanager.strategy.QuotationSource;

import java.time.LocalDate;
import java.util.*;

@Service
class AlphaVantageQuotationSource implements QuotationSource {

    private final AlphaVantageHistoricalService historicalService;


    AlphaVantageQuotationSource(AlphaVantageHistoricalService historicalService) {
        this.historicalService = historicalService;
    }

    @Override
    public List<Quotation> getQuotations(Ticker ticker) {
        Set<Quotation> quotations = new TreeSet<>();
        TimeSeriesResponse response = historicalService.getDailyHistory(ticker.getCode(), true);
        Map<String, DailyQuote> dailyTimeSeries = response.getDailyTimeSeries();
     for (String dateString : dailyTimeSeries.keySet()) {
            DailyQuote dailyQuote = dailyTimeSeries.get(dateString);
            Quotation quotation = new Quotation();
            quotation.setTicker(ticker);
            quotation.setDate(LocalDate.parse(dateString));
            quotation.setClosePrice(Double.valueOf(dailyQuote.getClose()));
            quotations.add(quotation);
        }
        // Implementation to fetch quotations from Alpha Vantage API
        return new ArrayList<>(quotations);
    }
}
