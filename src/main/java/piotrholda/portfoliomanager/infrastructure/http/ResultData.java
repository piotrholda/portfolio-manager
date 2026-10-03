package piotrholda.portfoliomanager.infrastructure.http;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;
import piotrholda.portfoliomanager.Ticker;
import piotrholda.portfoliomanager.strategy.Quotation;
import piotrholda.portfoliomanager.strategy.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** HTTP DTOs shared by strategy and simulation results. */
public final class ResultData {
    private ResultData() {
    }

    @Value
    public static class TickerData {
        String code;
        String exchangeCode;
        String currencyCode;

        static TickerData from(Ticker ticker) {
            return new TickerData(ticker.getCode(), ticker.getExchangeCode(), ticker.getCurrencyCode());
        }
    }

    @Value
    public static class Point {
        LocalDate date;
        @Schema(description = "Unrounded decimal value; units are documented on the containing series")
        BigDecimal value;
    }

    @Value
    public static class Series {
        TickerData ticker;
        List<Point> points;
    }

    @Value
    public static class TransactionData {
        LocalDate date;
        @Schema(allowableValues = {"BUY", "SELL"})
        String transactionType;
        TickerData ticker;
    }

    public static List<Point> points(List<Quotation> quotations) {
        return quotations.stream().sorted()
                .map(quotation -> new Point(quotation.getDate(), quotation.getClosePrice()))
                .collect(Collectors.toList());
    }

    public static List<Series> series(Map<Ticker, List<Quotation>> quotations) {
        Comparator<String> textOrder = Comparator.nullsFirst(Comparator.naturalOrder());
        Comparator<Ticker> tickerOrder = Comparator.comparing(Ticker::getCode, textOrder)
                .thenComparing(Ticker::getExchangeCode, textOrder)
                .thenComparing(Ticker::getCurrencyCode, textOrder);
        return quotations.entrySet().stream().sorted(Map.Entry.comparingByKey(tickerOrder))
                .map(entry -> new Series(TickerData.from(entry.getKey()), points(entry.getValue())))
                .collect(Collectors.toList());
    }

    public static List<TransactionData> transactions(List<Transaction> transactions) {
        return transactions.stream().sorted()
                .map(transaction -> new TransactionData(transaction.getDate(), String.valueOf(transaction.getTransactionType()),
                        TickerData.from(transaction.getTicker())))
                .collect(Collectors.toList());
    }
}
