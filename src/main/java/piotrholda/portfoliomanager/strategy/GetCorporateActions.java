package piotrholda.portfoliomanager.strategy;

import piotrholda.portfoliomanager.Ticker;
import piotrholda.portfoliomanager.corporateaction.CorporateAction;

import java.util.List;

public interface GetCorporateActions {
    List<CorporateAction> get(Ticker ticker);
}
