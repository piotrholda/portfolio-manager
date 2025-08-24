package piotrholda.portfoliomanager.quotation;

import piotrholda.portfoliomanager.strategy.Quotation;

import java.util.Collection;

public interface SaveQuotations {
    void save(Collection<Quotation> quotations);
}
