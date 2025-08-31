package piotrholda.portfoliomanager.strategy;

import lombok.Value;
import piotrholda.portfoliomanager.Ticker;

import java.time.LocalDate;

@Value
public class Transaction implements Comparable<Transaction>{
    LocalDate date;
    TransactionType transactionType;
    Ticker ticker;

    @Override
    public int compareTo(Transaction other) {
        return date.compareTo(other.date);
    }
}
