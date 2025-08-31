package piotrholda.portfoliomanager.corporateaction.out.persistence;

import piotrholda.portfoliomanager.Ticker;
import piotrholda.portfoliomanager.corporateaction.CorporateAction;
import piotrholda.portfoliomanager.corporateaction.CorporateActionType;
import piotrholda.portfoliomanager.corporateaction.Dividend;
import piotrholda.portfoliomanager.corporateaction.Split;

class CorporateActionMapper {
    static CorporateActionEntity toEntity(CorporateAction corporateAction) {
        CorporateActionMapperVisitor visitor = new CorporateActionMapperVisitor();
        corporateAction.accept(visitor);
        return visitor.getEntity();
    }

    static CorporateAction toDomain(CorporateActionEntity entity) {
        if (CorporateActionType.DIVIDEND.equals(entity.getType())) {
            return toDividend(entity);
        } else {
            return toSplit(entity);
        }
    }

    private static CorporateAction toDividend(CorporateActionEntity entity) {
        Dividend dividend = new Dividend();
        dividend.setTicker(Ticker.builder().code(entity.getCode()).exchangeCode(entity.getExchangeCode()).currencyCode(entity.getCurrencyCode()).build());
        dividend.setExDividendDate(entity.getExDividendDate());
        dividend.setPayableDate(entity.getPayableDate());
        dividend.setAmount(entity.getDividendAmount());
        dividend.setCurrency(entity.getDividendCurrency());
        return dividend;
    }

    private static CorporateAction toSplit(CorporateActionEntity entity) {
        Split split = new Split();
        split.setTicker(Ticker.builder().code(entity.getCode()).exchangeCode(entity.getExchangeCode()).currencyCode(entity.getCurrencyCode()).build());
        split.setDate(entity.getSplitDate());
        split.setRatio(entity.getSplitRatio());
        return split;
    }
}
