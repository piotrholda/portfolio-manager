package piotrholda.portfoliomanager.corporateaction.in.http;

import piotrholda.portfoliomanager.corporateaction.CorporateAction;

class CorporateActionResponseMapper {

    private CorporateActionResponseMapper() {
    }

    static CorporateActionResponse toResponse(CorporateAction corporateAction) {
        return new CorporateActionResponse(
                corporateAction.getType(),
                corporateAction.getTicker().getCode(),
                corporateAction.getTicker().getExchangeCode(),
                corporateAction.getTicker().getCurrencyCode(),
                corporateAction.getDate(),
                corporateAction.getAmount(),
                corporateAction.getCurrency(),
                corporateAction.getRatio()
        );
    }
}
