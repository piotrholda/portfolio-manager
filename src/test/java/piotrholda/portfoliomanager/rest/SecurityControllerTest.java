package piotrholda.portfoliomanager.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.http.MediaType.APPLICATION_JSON;


@ActiveProfiles("test")
@SpringBootTest
class SecurityControllerTest {

    @Autowired
    private SecurityController securityController;

    @Test
    void should() {

        // given
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

        // when/then
        WebTestClient
                .bindToController(securityController)
                .build()
                .post()
                .uri("/v1/security")
                .contentType(APPLICATION_JSON)
                .bodyValue(response)
                .exchange()
                .expectStatus().isOk()
                .expectBody().json(response);
    }

}
