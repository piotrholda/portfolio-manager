package piotrholda.portfoliomanager.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import piotrholda.portfoliomanager.Ticker;
import piotrholda.portfoliomanager.corporateaction.CorporateAction;

import java.util.List;

@Service
@RequiredArgsConstructor
class ExecuteDualEquityMomentumUseCase implements ExecuteDualEquityMomentum {

    private final GetQuotations getQuotations;
    private final GetCorporateActions getCorporateActions;
    private final AdjustQuotations adjustQuotations;

    @Override
    public Strategy execute(DualEquityMomentumParams params) {
        DualEquityMomentum strategy = new DualEquityMomentum();
        strategy.setCurrencyCode(params.getCurrencyCode());
        strategy.setBenchmark(params.getBenchmark());
        strategy.setLookBackPeriod(params.getLookBackPeriod());
        strategy.setRiskOffLookBackPeriod(params.getRiskOffLookBackPeriod());
        strategy.setRiskOn(params.getRiskOn());
        strategy.setRiskFree(params.getRiskFree());
        strategy.setRiskOff(params.getRiskOff());

        List<Quotation> benchmarkQuotations = getQuotations.getQuotations(strategy.getBenchmark());
        List<CorporateAction> benchmarkCorporateActions = getCorporateActions.get(strategy.getBenchmark());
        List<Quotation> benchmarkAdjustedQuotations = adjustQuotations.adjust(benchmarkQuotations, benchmarkCorporateActions);
        strategy.getQuotations().put(strategy.getBenchmark(), benchmarkAdjustedQuotations);
        List<Quotation> riskFreeQuotations = getQuotations.getQuotations(strategy.getRiskFree());
        List<CorporateAction> riskFreeCorporateActions = getCorporateActions.get(strategy.getRiskFree());
        List<Quotation> riskFreeAdjustedQuotations = adjustQuotations.adjust(riskFreeQuotations, riskFreeCorporateActions);
        strategy.getQuotations().put(strategy.getRiskFree(), riskFreeAdjustedQuotations);
        for (Ticker ticker : params.getRiskOn()) {
            List<Quotation> rickOnQuotations = getQuotations.getQuotations(ticker);
            List<CorporateAction> riskOnCorporateActions = getCorporateActions.get(ticker);
            List<Quotation> riskOnAdjustedQuotations = adjustQuotations.adjust(rickOnQuotations, riskOnCorporateActions);
            strategy.getQuotations().put(ticker, riskOnAdjustedQuotations);
        }
        for (Ticker ticker : params.getRiskOff()) {
            List<Quotation> riskOffQuotations = getQuotations.getQuotations(ticker);
            List<CorporateAction> riskOffCorporateActions = getCorporateActions.get(ticker);
            List<Quotation> riskOffAdjustedQuotations = adjustQuotations.adjust(riskOffQuotations, riskOffCorporateActions);
            strategy.getQuotations().put(ticker, riskOffAdjustedQuotations);
        }

        strategy.execute();

        return strategy;
    }
}
