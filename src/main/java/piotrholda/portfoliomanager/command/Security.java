package piotrholda.portfoliomanager.command;

import lombok.NoArgsConstructor;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateMember;
import org.axonframework.spring.stereotype.Aggregate;
import piotrholda.portfoliomanager.SecurityCreatedEvent;
import piotrholda.portfoliomanager.SecurityType;
import piotrholda.portfoliomanager.Ticker;

import java.util.UUID;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Aggregate
@NoArgsConstructor
class Security {

    @AggregateIdentifier
    private UUID securityId;
    SecurityType type;
    String name;
    @AggregateMember
    Ticker googleTicker;

    @CommandHandler
    Security(CreateSecurityCommand command) {
        apply(new SecurityCreatedEvent(command.getSecurityId(), command.getType(), command.getName(), command.getGoogleTicker()));
    }

    @EventHandler
    void on(SecurityCreatedEvent event) {
        securityId = event.getSecurityId();
        type = event.getType();
        name = event.getName();
        googleTicker = event.getGoogleTicker();
    }

}
