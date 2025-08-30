package piotrholda.portfoliomanager.corporateaction.out.persistence;

import lombok.Getter;
import piotrholda.portfoliomanager.corporateaction.CorporateActionType;
import piotrholda.portfoliomanager.corporateaction.CorporateActionVisitor;
import piotrholda.portfoliomanager.corporateaction.Dividend;
import piotrholda.portfoliomanager.corporateaction.Split;

@Getter
class CorporateActionMapperVisitor implements CorporateActionVisitor {
    private CorporateActionEntity entity;

    @Override
    public void visit(Dividend dividend) {
        entity = CorporateActionEntity.builder()
                .type(CorporateActionType.DIVIDEND)
                .code(dividend.getTicker().getCode())
                .exchangeCode(dividend.getTicker().getExchangeCode())
                .currencyCode(dividend.getTicker().getCurrencyCode())
                .exDividendDate(dividend.getExDividendDate())
                .payableDate(dividend.getPayableDate())
                .dividendAmount(dividend.getAmount())
                .dividendCurrency(dividend.getCurrency())
                .build();
    }

    @Override
    public void visit(Split split) {
        entity = CorporateActionEntity.builder()
                .type(CorporateActionType.SPLIT)
                .code(split.getTicker().getCode())
                .exchangeCode(split.getTicker().getExchangeCode())
                .currencyCode(split.getTicker().getCurrencyCode())
                .splitDate(split.getDate())
                .splitRatio(split.getRatio())
                .build();
    }
}
