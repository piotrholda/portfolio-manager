package piotrholda.portfoliomanager.corporateaction.out.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import piotrholda.portfoliomanager.Ticker;
import piotrholda.portfoliomanager.corporateaction.CorporateAction;
import piotrholda.portfoliomanager.strategy.GetCorporateActions;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class GetCorporateActionsUseCase implements GetCorporateActions {

    private final CorporateActionRepository repository;

    @Override
    public List<CorporateAction> get(Ticker ticker) {
        return repository.findAllByCodeAndExchangeCodeAndCurrencyCode(ticker.getCode(), ticker.getExchangeCode(), ticker.getCurrencyCode()).stream()
                .map(CorporateActionMapper::toDomain)
                .sorted()
                .collect(Collectors.toList());
    }
}
