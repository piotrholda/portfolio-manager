package piotrholda.portfoliomanager.query;

import lombok.Value;

import java.util.UUID;

@Value
public class FindSecurityQuery {
    UUID securityId;
}
