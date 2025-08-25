package piotrholda.portfoliomanager.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import piotrholda.portfoliomanager.Ticker;

@Service
@RequiredArgsConstructor
class ExecuteDualEquityMomentumUseCase implements ExecuteDualEquityMomentum {

    private final GetQuotations getQuotations;

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

        strategy.getQuotations().put(strategy.getBenchmark(), getQuotations.getQuotations(strategy.getBenchmark()));
        strategy.getQuotations().put(strategy.getRiskFree(), getQuotations.getQuotations(strategy.getRiskFree()));
        for (Ticker ticker : params.getRiskOn()) {
            strategy.getQuotations().put(ticker, getQuotations.getQuotations(ticker));
        }
        for (Ticker ticker : params.getRiskOff()) {
            strategy.getQuotations().put(ticker, getQuotations.getQuotations(ticker));
        }

        strategy.execute();

        return strategy;
    }
}
