package piotrholda.portfoliomanager.quotation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import piotrholda.portfoliomanager.Ticker;
import piotrholda.portfoliomanager.strategy.Quotation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
class ImportQuotationUseCase implements ImportQuotation {

    private final AlphaVantageHistoricalService historicalService;
    private final SaveQuotations saveQuotations;

    @Override
    public void importQuotations(String code) {
        Ticker ticker = Ticker.builder().code(code).exchangeCode( "NYSE").currencyCode( "USD").build();
        List<Quotation> quotations = getQuotations(ticker);
        saveQuotations.save(quotations);
    }

    private List<Quotation> getQuotations(Ticker ticker) {
        Set<Quotation> quotations = new TreeSet<>();
        TimeSeriesResponse response = historicalService.getDailyHistory(ticker.getCode(), true);
        Map<String, DailyQuote> dailyTimeSeries = response.getDailyTimeSeries();
        for (String dateString : dailyTimeSeries.keySet()) {
            DailyQuote dailyQuote = dailyTimeSeries.get(dateString);
            Quotation quotation = new Quotation(ticker, LocalDate.parse(dateString), new BigDecimal(dailyQuote.getClose()));
            quotations.add(quotation);
        }
        return new ArrayList<>(quotations);
    }
}
