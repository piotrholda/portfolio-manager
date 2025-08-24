package piotrholda.portfoliomanager.quotation.persistence;

import piotrholda.portfoliomanager.Ticker;
import piotrholda.portfoliomanager.strategy.Quotation;

class QuotationMapper {

    static QuotationEntity toEntity(Quotation quotation) {
        QuotationEntity entity = new QuotationEntity();
        entity.setCode(quotation.getTicker().getCode());
        entity.setExchangeCode(quotation.getTicker().getExchangeCode());
        entity.setCurrencyCode(quotation.getTicker().getCurrencyCode());
        entity.setDate(quotation.getDate());
        entity.setClosePrice(quotation.getClosePrice());
        return entity;
    }

    static Quotation toDomain(QuotationEntity entity) {
        Quotation quotation = new Quotation();
        quotation.setTicker(new Ticker(entity.getCode(), entity.getExchangeCode(), entity.getCurrencyCode()));
                quotation.setDate(entity.getDate());
                quotation.setClosePrice(entity.getClosePrice());
        return quotation;
    }
}
