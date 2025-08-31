package piotrholda.portfoliomanager.command;

import org.axonframework.test.aggregate.AggregateTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import piotrholda.portfoliomanager.SecurityCreatedEvent;
import piotrholda.portfoliomanager.SecurityType;
import piotrholda.portfoliomanager.Ticker;

import java.util.UUID;


class SecurityTest {

    private AggregateTestFixture<Security> testFixture;

    @BeforeEach
    void setUp() {
        testFixture = new AggregateTestFixture<>(Security.class);
    }

    @Test
    void createSecurityTest() {
        UUID securityId = UUID.randomUUID();
        CreateSecurityCommand CreateSecurityCommand = new CreateSecurityCommand(securityId,
                SecurityType.ETF,
                "Vanguard FTSE Emerging Markets UCITS ETF USD",
                Ticker.builder().code("VDEM").exchangeCode("LON").currencyCode("USD").build());
        SecurityCreatedEvent securityCreatedEvent = new SecurityCreatedEvent(securityId,
                SecurityType.ETF,
                "Vanguard FTSE Emerging Markets UCITS ETF USD",
                Ticker.builder().code("VDEM").exchangeCode("LON").currencyCode("USD").build());

        testFixture.givenNoPriorActivity()
                .when(CreateSecurityCommand)
                .expectEvents(securityCreatedEvent)
                .expectSuccessfulHandlerExecution();
    }

}
