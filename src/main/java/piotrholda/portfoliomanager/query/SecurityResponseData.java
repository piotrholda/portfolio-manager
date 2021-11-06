package piotrholda.portfoliomanager.query;

import lombok.Value;
import piotrholda.portfoliomanager.SecurityType;
import piotrholda.portfoliomanager.Ticker;

@Value
public class SecurityResponseData {
    String securityId;
    SecurityType type;
    String name;
    Ticker googleTicker;
}
