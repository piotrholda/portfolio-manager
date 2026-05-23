package piotrholda.portfoliomanager.quotation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import piotrholda.portfoliomanager.Ticker;
import piotrholda.portfoliomanager.strategy.Quotation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
class ImportQuotationUseCase implements ImportQuotation {

    private final AlphaVantageHistoricalService historicalService;
    private final StooqHistoricalService stooqHistoricalService;
    private final SaveQuotations saveQuotations;

    @Override
    public void importQuotations(Ticker ticker) {
        log.info("Starting quotation import for code={}, exchangeCode={}, currencyCode={}",
                ticker.getCode(), ticker.getExchangeCode(), ticker.getCurrencyCode());
        List<Quotation> quotations = getQuotations(ticker);
        log.info("Fetched {} quotations for code={}, exchangeCode={}, currencyCode={}",
                quotations.size(), ticker.getCode(), ticker.getExchangeCode(), ticker.getCurrencyCode());
        saveQuotations.save(quotations);
        log.info("Finished quotation import for code={}, exchangeCode={}, currencyCode={}",
                ticker.getCode(), ticker.getExchangeCode(), ticker.getCurrencyCode());
    }

    private List<Quotation> getQuotations(Ticker ticker) {
        if ("WSE".equalsIgnoreCase(ticker.getExchangeCode())) {
            log.info("Using Stooq as quotation source for ticker {}", ticker.getCode());
            return getStooqQuotations(ticker);
        }
        log.info("Using Alpha Vantage as quotation source for ticker {}", ticker.getCode());
        return getAlphaVantageQuotations(ticker);
    }

    private List<Quotation> getAlphaVantageQuotations(Ticker ticker) {
        Set<Quotation> quotations = new TreeSet<>();
        TimeSeriesResponse response = historicalService.getDailyHistory(ticker.getCode(), true);
        if (response == null || response.getDailyTimeSeries() == null) {
            log.warn("Alpha Vantage returned no daily quotations for code={}, exchangeCode={}",
                    ticker.getCode(), ticker.getExchangeCode());
            return new ArrayList<>();
        }
        Map<String, DailyQuote> dailyTimeSeries = response.getDailyTimeSeries();
        for (String dateString : dailyTimeSeries.keySet()) {
            DailyQuote dailyQuote = dailyTimeSeries.get(dateString);
            Quotation quotation = new Quotation(ticker, LocalDate.parse(dateString), new BigDecimal(dailyQuote.getClose()));
            quotations.add(quotation);
        }
        return new ArrayList<>(quotations);
    }

    private List<Quotation> getStooqQuotations(Ticker ticker) {
        Set<Quotation> quotations = new TreeSet<>();
        String stooqSymbol = ticker.getCode().concat(".pl");
        log.info("Resolved Stooq symbol {} for code={}", stooqSymbol, ticker.getCode());
        for (StooqHistoricalService.StooqDailyQuote dailyQuote : stooqHistoricalService.getDailyHistory(stooqSymbol)) {
            quotations.add(new Quotation(ticker, dailyQuote.getDate(), dailyQuote.getClose()));
        }
        return new ArrayList<>(quotations);
    }
}
