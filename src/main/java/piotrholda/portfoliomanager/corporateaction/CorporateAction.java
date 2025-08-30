package piotrholda.portfoliomanager.corporateaction;

import piotrholda.portfoliomanager.Ticker;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface CorporateAction {
    CorporateActionType getType();
    Ticker getTicker();
    LocalDate getDate();
    BigDecimal getAmount();
    String getCurrency();
    BigDecimal getRatio();

    void accept(CorporateActionVisitor visitor);
}
