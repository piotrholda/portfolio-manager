package piotrholda.portfoliomanager.corporateaction;

import java.util.Collection;

public interface FetchCorporateActions {
    Collection<CorporateAction> get(String code);
}
