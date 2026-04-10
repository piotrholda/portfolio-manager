package piotrholda.portfoliomanager.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import piotrholda.portfoliomanager.SecurityType;
import piotrholda.portfoliomanager.Ticker;
import piotrholda.portfoliomanager.query.SecurityResponseData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@ActiveProfiles("test")
@SpringBootTest
class SecurityControllerTest {

    @Autowired
    private SecurityController securityController;

    @Test
    void shouldCreateAndExposeSecurityEndpoints() {
        WebTestClient webTestClient = WebTestClient.bindToController(securityController).build();

        SecurityRequestData request = new SecurityRequestData();
        request.setName("Verizon Communications Inc.");
        request.setType(SecurityType.SHARE);
        request.setGoogleTicker(Ticker.builder()
                .code("VZ")
                .exchangeCode("NYSE")
                .currencyCode("USD")
                .build());

        SecurityResponseData created = webTestClient
                .post()
                .uri("/v1/security")
                .contentType(APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(SecurityResponseData.class)
                .returnResult()
                .getResponseBody();

        assertThat(created).isNotNull();
        assertThat(created.getSecurityId()).isNotBlank();

        SecurityResponseData fetched = webTestClient
                .get()
                .uri("/v1/security/" + created.getSecurityId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(SecurityResponseData.class)
                .returnResult()
                .getResponseBody();

        assertThat(fetched).isEqualTo(created);

        List<SecurityResponseData> securities = webTestClient
                .get()
                .uri("/v1/security")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(SecurityResponseData.class)
                .returnResult()
                .getResponseBody();

        assertThat(securities).containsExactly(created);
    }
}
