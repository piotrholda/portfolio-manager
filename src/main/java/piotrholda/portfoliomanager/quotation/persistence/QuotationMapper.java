package piotrholda.portfoliomanager.quotation.persistence;

import piotrholda.portfoliomanager.Ticker;
import piotrholda.portfoliomanager.strategy.Quotation;

import java.util.UUID;

class QuotationMapper {

    static QuotationEntity toEntity(Quotation quotation) {
        QuotationEntity entity = new QuotationEntity();
        entity.setQuotationId(UUID.randomUUID().toString());
        entity.setCode(quotation.getTicker().getCode());
        entity.setExchangeCode(quotation.getTicker().getExchangeCode());
        entity.setCurrencyCode(quotation.getTicker().getCurrencyCode());
        entity.setDate(quotation.getDate());
        entity.setClosePrice(quotation.getClosePrice());
        return entity;
    }

    static Quotation toDomain(QuotationEntity entity) {
        return new Quotation(
                Ticker.builder().code(entity.getCode()).exchangeCode(entity.getExchangeCode()).currencyCode(entity.getCurrencyCode()).build(),
                entity.getDate(),
                entity.getClosePrice());
    }
}
