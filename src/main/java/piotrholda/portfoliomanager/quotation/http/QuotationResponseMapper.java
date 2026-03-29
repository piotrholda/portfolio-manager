package piotrholda.portfoliomanager.quotation.http;

import piotrholda.portfoliomanager.strategy.Quotation;

class QuotationResponseMapper {

    private QuotationResponseMapper() {
    }

    static QuotationResponse toResponse(Quotation quotation) {
        return new QuotationResponse(
                quotation.getTicker().getCode(),
                quotation.getTicker().getExchangeCode(),
                quotation.getTicker().getCurrencyCode(),
                quotation.getDate(),
                quotation.getClosePrice()
        );
    }
}
