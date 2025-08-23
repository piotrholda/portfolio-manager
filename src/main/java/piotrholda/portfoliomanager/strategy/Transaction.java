package piotrholda.portfoliomanager.strategy;

import lombok.Value;
import piotrholda.portfoliomanager.Ticker;

import java.time.LocalDate;

@Value
class Transaction {
    LocalDate date;
    TransactionType transactionType;
    Ticker ticker;
}
