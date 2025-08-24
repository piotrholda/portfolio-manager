package piotrholda.portfoliomanager.quotation;

import lombok.extern.slf4j.Slf4j;
import piotrholda.portfoliomanager.Ticker;
import piotrholda.portfoliomanager.strategy.Quotation;
import piotrholda.portfoliomanager.strategy.GetQuotations;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@Deprecated
@Slf4j
class YahooQuotationSource implements GetQuotations {
    @Override
    public List<Quotation> getQuotations(Ticker ticker) {
        long apiDelayMs = 100L;
        Set<Quotation> quotations = new TreeSet<>();
            Stock stock = getStock(ticker);
            int year = java.time.LocalDate.now().getYear();
            GregorianCalendar from = new GregorianCalendar(year, Calendar.JANUARY, 1);
            Calendar to = new GregorianCalendar();
            List<HistoricalQuote> history;
            do {
                history = fetchHistory(stock, from, to, apiDelayMs);
                for (HistoricalQuote quote : history) {
                    if (quote.getClose() != null) {
                        LocalDate date = LocalDate.of(quote.getDate().get(Calendar.YEAR),
                                quote.getDate().get(Calendar.MONTH) + 1,
                                quote.getDate().get(Calendar.DAY_OF_MONTH));
                        Quotation quotation = new Quotation();
                        quotation.setTicker(ticker);
                        quotation.setDate(date);
                        quotation.setClosePrice(quote.getClose().doubleValue());
                        quotations.add(quotation);
                    }
                }
                from = new GregorianCalendar(from.get(Calendar.YEAR) - 1, Calendar.JANUARY, 1);
                to = new GregorianCalendar(to.get(Calendar.YEAR) - 1, Calendar.DECEMBER, 31);
                waitForApi(apiDelayMs);
            } while (!history.isEmpty());
        return new ArrayList<>(quotations);
    }

    private static Stock getStock(Ticker ticker) {
        do {
            try {
                return YahooFinance.get(ticker.getCode());
            } catch (IOException e) {
                log.info("Wait for Yahoo Finance API in getStock...");
                waitForApi(100L);
            }
        } while (true);
    }

    private static List<HistoricalQuote> fetchHistory(Stock stock, GregorianCalendar from, Calendar to, long apiDelayMs)  {
        do {
            try {
                return stock.getHistory(from, to, Interval.DAILY);
            } catch (IOException e) {
                log.info("Wait for Yahoo Finance API in fetchHistory...");
                waitForApi(apiDelayMs);
            }
        } while (true);
    }

    private static void waitForApi(long apiDelayMs) {
        try {
        Thread.sleep(apiDelayMs);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
