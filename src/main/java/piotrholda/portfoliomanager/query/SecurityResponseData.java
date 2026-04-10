package piotrholda.portfoliomanager.query;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import piotrholda.portfoliomanager.SecurityType;
import piotrholda.portfoliomanager.Ticker;

@Value
@Jacksonized
@Builder
public class SecurityResponseData {
    String securityId;
    SecurityType type;
    String name;
    Ticker googleTicker;
}
