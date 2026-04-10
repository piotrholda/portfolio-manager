package piotrholda.portfoliomanager.quotation.http;

import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;

@Value
class QuotationResponse {
    String code;
    String exchangeCode;
    String currencyCode;
    LocalDate date;
    BigDecimal closePrice;
}
