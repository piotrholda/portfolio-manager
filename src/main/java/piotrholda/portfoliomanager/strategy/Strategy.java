package piotrholda.portfoliomanager.strategy;

import java.util.List;

public interface Strategy {

    void execute();
    List<Transaction> getTransactions();
}
