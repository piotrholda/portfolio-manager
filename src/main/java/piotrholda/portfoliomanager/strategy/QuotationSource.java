package piotrholda.portfoliomanager.strategy;

import piotrholda.portfoliomanager.Ticker;

import java.util.List;

public interface QuotationSource {

    List<Quotation> getQuotations(Ticker ticker);
}
