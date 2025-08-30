package piotrholda.portfoliomanager.strategy.http;

import lombok.Data;
import piotrholda.portfoliomanager.strategy.DualEquityMomentumParams;

import java.util.Collection;

import static java.util.stream.Collectors.toList;

@Data
class DualEquityMomentumRequest {
    private String currencyCode;
    private StrategyRequestTicker benchmark;
    private int lookBackPeriod;
    private int riskOffLookBackPeriod;
    private Collection<StrategyRequestTicker> riskOn;
    private StrategyRequestTicker riskFree;
    private Collection<StrategyRequestTicker> riskOff;

    DualEquityMomentumParams toParams() {
        DualEquityMomentumParams params = new DualEquityMomentumParams();
        params.setCurrencyCode(currencyCode);
        params.setBenchmark(benchmark.toTicker());
        params.setLookBackPeriod(lookBackPeriod);
        params.setRiskOffLookBackPeriod(riskOffLookBackPeriod);
        params.setRiskOn(riskOn.stream().map(StrategyRequestTicker::toTicker).collect(toList()));
        params.setRiskFree(riskFree.toTicker());
        params.setRiskOff(riskOff.stream().map(StrategyRequestTicker::toTicker).collect(toList()));
        return params;
    }
}
