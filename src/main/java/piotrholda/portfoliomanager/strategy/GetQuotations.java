package piotrholda.portfoliomanager.strategy;

import piotrholda.portfoliomanager.Ticker;

import java.util.List;

public interface GetQuotations {

    List<Quotation> getQuotations(Ticker ticker);
}
