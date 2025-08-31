package piotrholda.portfoliomanager.simulation;

import lombok.Data;
import piotrholda.portfoliomanager.Ticker;
import piotrholda.portfoliomanager.infrastructure.Math;
import piotrholda.portfoliomanager.strategy.Quotation;
import piotrholda.portfoliomanager.strategy.Strategy;
import piotrholda.portfoliomanager.strategy.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Data
public class Simulation {
    private Strategy strategy;

    private List<Quotation> unnormalisedResults = new ArrayList<>();
    private Map<Ticker, List<Quotation>> quotations;
    private List<Quotation> results = new ArrayList<>();

    void execute() {
        simulate();
        normalise();
    }

    private void simulate() {
        BigDecimal capital = BigDecimal.valueOf(1000);
        List<Transaction> transactions = strategy.getTransactions();
        unnormalisedResults.add(new Quotation(transactions.get(0).getTicker(), transactions.get(0).getDate(), capital));
        for (Transaction transaction : transactions) {
            Optional<Transaction> nextTransaction = transactions.stream()
                    .filter(t -> t.getDate().isAfter(transaction.getDate()))
                    .findFirst();
            List<Quotation> strategyQuotations = strategy.getQuotations().get(transaction.getTicker());
            BigDecimal startCapital = new BigDecimal(capital.toString());
            BigDecimal startClosePrice = strategyQuotations.stream()
                    .filter(q -> !q.getDate().isAfter(transaction.getDate()))
                    .map(Quotation::getClosePrice)
                    .reduce((first, second) -> second)
                    .orElseThrow();
            for (Quotation quotation : strategyQuotations) {
                if (quotation.getDate().isAfter(transaction.getDate()) && (nextTransaction.isEmpty() || !quotation.getDate().isAfter(nextTransaction.get().getDate()))) {
                    BigDecimal closePrice = quotation.getClosePrice().multiply(startCapital).divide(startClosePrice, Math.MATH_CONTEXT);
                    capital = closePrice;
                    unnormalisedResults.add(new Quotation(quotation.getTicker(), quotation.getDate(), closePrice));
                }
            }
        }
    }

    private void normalise() {
        quotations = new HashMap<>();
        for (Ticker ticker : strategy.getQuotations().keySet()) {
            quotations.put(ticker, new ArrayList<>());
        }
        results = new ArrayList<>();
        LocalDate firstDate = unnormalisedResults.get(0).getDate();
        BigDecimal firstResultPrice = unnormalisedResults.get(0).getClosePrice();
        Map<Ticker, BigDecimal> firstPrices = new HashMap<>();
        for (Map.Entry<Ticker, List<Quotation>> entry : strategy.getQuotations().entrySet()) {
            BigDecimal firstPrice = findPrice(entry.getValue(), firstDate);
            firstPrices.put(entry.getKey(), firstPrice);
        }
        for (Quotation unnormalisedResult : unnormalisedResults) {
            LocalDate date = unnormalisedResult.getDate();
            BigDecimal price = unnormalisedResult.getClosePrice();
            BigDecimal normalisedPrice = percentChange(price, firstResultPrice);
            results.add(new Quotation(unnormalisedResult.getTicker(), date, normalisedPrice));
            for (Map.Entry<Ticker, List<Quotation>> entry : strategy.getQuotations().entrySet()) {
                Ticker ticker = entry.getKey();
                BigDecimal firstPrice = firstPrices.get(ticker);
                BigDecimal newPrice = findPrice(entry.getValue(), date);
                BigDecimal normalisedNewPrice = percentChange(newPrice, firstPrice);
                quotations.get(ticker).add(new Quotation(ticker, date, normalisedNewPrice));
            }
        }
    }

    private BigDecimal percentChange(BigDecimal newPrice, BigDecimal oldPrice) {
        return newPrice.subtract(oldPrice)
                .divide(oldPrice, Math.MATH_CONTEXT)
                .multiply(BigDecimal.valueOf(100));
    }

    private BigDecimal findPrice(List<Quotation> quotations, LocalDate date) {
        return quotations.stream().filter(q -> !q.getDate().isAfter(date))
                .map(Quotation::getClosePrice)
                .reduce((first, second) -> second)
                .orElseThrow();
    }

    public List<Transaction> getTransactions() {
        return strategy.getTransactions();
    }
}
