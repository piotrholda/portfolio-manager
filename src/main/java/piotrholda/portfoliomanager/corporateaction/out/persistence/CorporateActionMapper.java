package piotrholda.portfoliomanager.corporateaction.out.persistence;

import piotrholda.portfoliomanager.corporateaction.CorporateAction;

class CorporateActionMapper {
    static CorporateActionEntity toEntity(CorporateAction corporateAction) {
        CorporateActionMapperVisitor visitor = new CorporateActionMapperVisitor();
        corporateAction.accept(visitor);
        return visitor.getEntity();
    }
}
