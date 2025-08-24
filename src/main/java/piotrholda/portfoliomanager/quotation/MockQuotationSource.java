package piotrholda.portfoliomanager.quotation;

import piotrholda.portfoliomanager.Ticker;
import piotrholda.portfoliomanager.strategy.Quotation;
import piotrholda.portfoliomanager.strategy.GetQuotations;

import java.util.List;

class MockQuotationSource implements GetQuotations {
    @Override
    public List<Quotation> getQuotations(Ticker ticker) {
        return List.of();
    }
}
