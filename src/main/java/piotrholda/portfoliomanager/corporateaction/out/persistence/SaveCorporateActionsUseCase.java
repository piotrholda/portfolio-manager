package piotrholda.portfoliomanager.corporateaction.out.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import piotrholda.portfoliomanager.corporateaction.CorporateAction;
import piotrholda.portfoliomanager.corporateaction.CorporateActionType;
import piotrholda.portfoliomanager.corporateaction.SaveCorporateActions;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
class SaveCorporateActionsUseCase implements SaveCorporateActions {

    private final CorporateActionRepository repository;

    @Override
    public void save(Collection<CorporateAction> corporateActions) {
        for (CorporateAction corporateAction: corporateActions) {
            Optional<CorporateActionEntity> corporateActionEntity = CorporateActionType.DIVIDEND.equals(corporateAction.getType()) ? repository.findDividend(
                    corporateAction.getTicker().getCode(),
                    corporateAction.getTicker().getExchangeCode(),
                    corporateAction.getTicker().getCurrencyCode(),
                    corporateAction.getDate()
            ) : repository.findSplit(
                    corporateAction.getTicker().getCode(),
                    corporateAction.getTicker().getExchangeCode(),
                    corporateAction.getTicker().getCurrencyCode(),
                    corporateAction.getDate()
            );
            if (corporateActionEntity.isEmpty()) {
                CorporateActionEntity entity = CorporateActionMapper.toEntity(corporateAction);
                entity.setCorporateActionId(UUID.randomUUID().toString());
                repository.save(entity);
            } else {
                CorporateActionEntity entity = corporateActionEntity.get();
                entity.setDividendAmount(corporateAction.getAmount());
                entity.setSplitRatio(corporateAction.getRatio());
                repository.save(entity);
            }
        }
    }
}
