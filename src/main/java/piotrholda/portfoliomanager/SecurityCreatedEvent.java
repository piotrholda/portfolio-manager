package piotrholda.portfoliomanager;

import lombok.Value;

import java.util.UUID;

@Value
public class SecurityCreatedEvent {
    UUID securityId;
    SecurityType type;
    String name;
    Ticker googleTicker;
}
