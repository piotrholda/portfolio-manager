package piotrholda.portfoliomanager.simulation.in.http;

import lombok.Data;
import piotrholda.portfoliomanager.strategy.DualEquityMomentumParams;

import java.util.Collection;

import static java.util.stream.Collectors.toList;

@Data
class SimulateDualEquityMomentumRequest {
    private String currencyCode;
    private SimulateRequestTicker benchmark;
    private int lookBackPeriod;
    private int riskOffLookBackPeriod;
    private Collection<SimulateRequestTicker> riskOn;
    private SimulateRequestTicker riskFree;
    private Collection<SimulateRequestTicker> riskOff;
    private int skipMonths;

    DualEquityMomentumParams toParams() {
        DualEquityMomentumParams params = new DualEquityMomentumParams();
        params.setCurrencyCode(currencyCode);
        params.setBenchmark(benchmark.toTicker());
        params.setLookBackPeriod(lookBackPeriod);
        params.setRiskOffLookBackPeriod(riskOffLookBackPeriod);
        params.setRiskOn(riskOn.stream().map(SimulateRequestTicker::toTicker).collect(toList()));
        params.setRiskFree(riskFree.toTicker());
        params.setRiskOff(riskOff.stream().map(SimulateRequestTicker::toTicker).collect(toList()));
        params.setSkipMonths(skipMonths);
        return params;
    }
}
