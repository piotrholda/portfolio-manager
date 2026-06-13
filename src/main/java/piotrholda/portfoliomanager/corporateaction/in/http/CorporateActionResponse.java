package piotrholda.portfoliomanager.corporateaction.in.http;

import lombok.Value;
import piotrholda.portfoliomanager.corporateaction.CorporateActionType;

import java.math.BigDecimal;
import java.time.LocalDate;

@Value
class CorporateActionResponse {
    CorporateActionType type;
    String code;
    String exchangeCode;
    String currencyCode;
    LocalDate date;
    BigDecimal amount;
    String currency;
    BigDecimal ratio;
}
