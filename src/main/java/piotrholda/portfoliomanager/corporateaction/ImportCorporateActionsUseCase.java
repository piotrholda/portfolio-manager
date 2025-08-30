package piotrholda.portfoliomanager.corporateaction;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@RequiredArgsConstructor
class ImportCorporateActionsUseCase implements ImportCorporateActions {

    private final FetchCorporateActions fetchCorporateActions;
    private final SaveCorporateActions saveCorporateActions;

    @Override
    public void importCorporateActions(String code) {
        Collection<CorporateAction> corporateActions = fetchCorporateActions.get(code);
        saveCorporateActions.save(corporateActions);
    }
}
