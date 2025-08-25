package piotrholda.portfoliomanager.strategy;

import piotrholda.portfoliomanager.Ticker;

import java.util.List;
import java.util.Map;

public interface Strategy {

    void execute();
    List<Transaction> getTransactions();
    Map<Ticker, List<Quotation>> getQuotations();
}
