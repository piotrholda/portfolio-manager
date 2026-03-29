package piotrholda.portfoliomanager.rest;

import org.axonframework.extensions.reactor.commandhandling.gateway.ReactorCommandGateway;
import org.axonframework.extensions.reactor.queryhandling.gateway.ReactorQueryGateway;
import org.axonframework.messaging.responsetypes.ResponseType;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import piotrholda.portfoliomanager.SecurityType;
import piotrholda.portfoliomanager.Ticker;
import piotrholda.portfoliomanager.command.CreateSecurityCommand;
import piotrholda.portfoliomanager.query.FindSecurityQuery;
import piotrholda.portfoliomanager.query.SecurityResponseData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;

class SecurityControllerTest {

    @Test
    void should() {
        ReactorCommandGateway reactorCommandGateway = mock(ReactorCommandGateway.class);
        ReactorQueryGateway reactorQueryGateway = mock(ReactorQueryGateway.class);
        SecurityController securityController = new SecurityController(reactorCommandGateway, reactorQueryGateway);

        SecurityResponseData securityResponseData = new SecurityResponseData(
                "test-security-id",
                SecurityType.SHARE,
                "Verizon Communications Inc.",
                Ticker.builder().code("VZ").exchangeCode("NYSE").currencyCode("USD").build()
        );

        when(reactorCommandGateway.send(any(CreateSecurityCommand.class)))
                .thenReturn(Mono.just("ok"));
        when(reactorQueryGateway.queryUpdates(
                any(FindSecurityQuery.class),
                org.mockito.ArgumentMatchers.<ResponseType<SecurityResponseData>>any()))
                .thenReturn(Flux.just(securityResponseData));

        String request = "{\n" +
                "  \"googleTicker\": {\n" +
                "    \"code\": \"VZ\",\n" +
                "    \"currencyCode\": \"USD\",\n" +
                "    \"exchangeCode\": \"NYSE\"\n" +
                "  },\n" +
                "  \"name\": \"Verizon Communications Inc.\",\n" +
                "  \"type\": \"SHARE\"\n" +
                "}";
        String response = "{\n" +
                "  \"type\": \"SHARE\",\n" +
                "  \"name\": \"Verizon Communications Inc.\",\n" +
                "  \"googleTicker\": {\n" +
                "    \"code\": \"VZ\",\n" +
                "    \"exchangeCode\": \"NYSE\",\n" +
                "    \"currencyCode\": \"USD\"\n" +
                "  }\n" +
                "}";

        WebTestClient
                .bindToController(securityController)
                .build()
                .post()
                .uri("/v1/security")
                .contentType(APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody().json(response);
    }
}
