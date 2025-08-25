package piotrholda.portfoliomanager.strategy;

import lombok.Data;
import piotrholda.portfoliomanager.Ticker;

import java.util.Collection;

@Data
public class DualEquityMomentumParams {
    private String currencyCode;
    private Ticker benchmark;
    private int lookBackPeriod;
    private int riskOffLookBackPeriod;
    private Collection<Ticker> riskOn;
    private Ticker riskFree;
    private Collection<Ticker> riskOff;
}
