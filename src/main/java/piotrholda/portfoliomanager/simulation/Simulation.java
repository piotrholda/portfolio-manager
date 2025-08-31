package piotrholda.portfoliomanager.simulation;

import lombok.Data;
import piotrholda.portfoliomanager.Ticker;
import piotrholda.portfoliomanager.strategy.Quotation;
import piotrholda.portfoliomanager.strategy.Strategy;
import piotrholda.portfoliomanager.strategy.Transaction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
class Simulation {
    private Strategy strategy;

    private List<Quotation> unnormalisedResults;

    void execute() {
simulate();
    }

    private void simulate() {
        BigDecimal capital = BigDecimal.valueOf(100);
        List<Transaction> transactions = strategy.getTransactions().stream().sorted().collect(Collectors.toList());
        for (Transaction transaction : transactions) {
            Optional<Transaction> nextTransaction = transactions.stream()
                    .filter(t -> t.getDate().isAfter(transaction.getDate()))
                    .findFirst();
        }
    }

    List<Transaction> removeRedundantTransactions(List<Transaction> transactions) {
        List<Transaction> result = new ArrayList<>();
        for (Transaction transaction : transactions) {
            Optional<Transaction> previousTransaction = transactions.stream()
                    .filter(t -> t.getDate().isBefore(transaction.getDate()))
                    .reduce((first, second) -> second);
            if (previousTransaction.isPresent() && previousTransaction.get().getTicker().equals(transaction.getTicker())) {
                continue;
            }
            result.add(transaction);
        }
        return result;
    }

    Map<Ticker, List<Quotation>> getQuotations() {
        return Map.of();
    }
    List<Transaction> getTransactions() {
        return List.of();
    }

    List<Quotation> getResults() {
        return List.of();
    }

}
