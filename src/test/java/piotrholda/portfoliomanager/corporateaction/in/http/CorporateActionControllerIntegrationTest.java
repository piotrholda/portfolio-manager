package piotrholda.portfoliomanager.corporateaction.in.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CorporateActionControllerIntegrationTest {

    private static HttpServer stockApiStub;
    private static String stockApiBasePath;
    private static final AtomicReference<String> requestedPath = new AtomicReference<>();

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @AfterAll
    static void stopStub() {
        if (stockApiStub != null) {
            stockApiStub.stop(0);
        }
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        startStub();
        registry.add("stock-api.base-path", () -> stockApiBasePath);
    }

    @Test
    void shouldImportCorporateActions() {
        requestedPath.set(null);

        var importResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/v1/corporateaction/import",
                Map.of("code", "VT"),
                Void.class
        );

        assertEquals(204, importResponse.getStatusCodeValue());
        assertEquals("/api/stock-data?ticker=VT", requestedPath.get());

        var corporateActionsResponse = restTemplate.getForEntity(
                "http://localhost:" + port + "/v1/corporateaction?code=VT",
                String.class
        );

        assertEquals(200, corporateActionsResponse.getStatusCodeValue());
        assertNotNull(corporateActionsResponse.getBody());
        assertTrue(corporateActionsResponse.getBody().startsWith("["));
        assertTrue(corporateActionsResponse.getBody().contains("\"type\":\"DIVIDEND\""));
        assertTrue(corporateActionsResponse.getBody().contains("\"code\":\"VT\""));
        assertTrue(corporateActionsResponse.getBody().contains("\"exchangeCode\":\"NYSE\""));
        assertTrue(corporateActionsResponse.getBody().contains("\"currencyCode\":\"USD\""));
        assertTrue(corporateActionsResponse.getBody().contains("\"date\":\"2024-03-15\""));
        assertTrue(corporateActionsResponse.getBody().contains("\"amount\":0.420000000000"));
        assertTrue(corporateActionsResponse.getBody().contains("\"currency\":\"USD\""));
        assertTrue(corporateActionsResponse.getBody().contains("\"type\":\"SPLIT\""));
        assertTrue(corporateActionsResponse.getBody().contains("\"date\":\"2024-05-01\""));
        assertTrue(corporateActionsResponse.getBody().contains("\"ratio\":2.000000000000"));
    }

    @Test
    void shouldReturnInternalServerErrorWhenImportFails() {
        requestedPath.set(null);

        var importResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/v1/corporateaction/import",
                Map.of("code", "FAIL"),
                String.class
        );

        assertEquals(500, importResponse.getStatusCodeValue());
        assertEquals("/api/stock-data?ticker=FAIL", requestedPath.get());
        assertNotNull(importResponse.getBody());
        assertTrue(importResponse.getBody().contains("Error initiating corporate actions import"));
        assertTrue(importResponse.getBody().contains("Failed to fetch stock data for ticker: FAIL"));
    }

    private static void handleStockDataRequest(HttpExchange exchange) throws IOException {
        requestedPath.set(exchange.getRequestURI().toString());

        if (requestedPath.get().contains("ticker=FAIL")) {
            byte[] responseBytes = "{\"detail\":\"Stock API failure\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(500, responseBytes.length);
            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(responseBytes);
            }
            return;
        }

        String response = "{"
                + "\"ticker\":\"VT\","
                + "\"dividends\":[{"
                + "\"Ex-dividend date\":\"2024-03-15\","
                + "\"Payable date\":\"2024-04-02\","
                + "\"Dividend amount (change)\":\"0.42 USD\","
                + "\"Adjusted Price\":\"100.00\","
                + "\"Close Price\":\"101.00\""
                + "}],"
                + "\"splits\":[{"
                + "\"Date\":\"2024-05-01\","
                + "\"Split Ratio\":\"2.0\""
                + "}]"
                + "}";
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, responseBytes.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(responseBytes);
        }
    }

    private static void startStub() {
        if (stockApiStub != null) {
            return;
        }
        try {
            stockApiStub = HttpServer.create(new InetSocketAddress(0), 0);
            stockApiStub.createContext("/api/stock-data", CorporateActionControllerIntegrationTest::handleStockDataRequest);
            stockApiStub.start();
            stockApiBasePath = "http://localhost:" + stockApiStub.getAddress().getPort();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to start stock API stub server", e);
        }
    }
}
