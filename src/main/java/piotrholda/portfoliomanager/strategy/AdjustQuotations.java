package piotrholda.portfoliomanager.strategy;

import piotrholda.portfoliomanager.corporateaction.CorporateAction;

import java.util.List;

public interface AdjustQuotations {
    List<Quotation> adjust(List<Quotation> quotations, List<CorporateAction> corporateActions);
}
