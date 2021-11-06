package piotrholda.portfoliomanager.command;

import lombok.Value;
import org.axonframework.modelling.command.TargetAggregateIdentifier;
import piotrholda.portfoliomanager.SecurityType;
import piotrholda.portfoliomanager.Ticker;

import java.util.UUID;

@Value
public class CreateSecurityCommand {

    @TargetAggregateIdentifier
    UUID securityId;
    SecurityType type;
    String name;
    Ticker googleTicker;

}
