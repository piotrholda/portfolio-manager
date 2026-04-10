package piotrholda.portfoliomanager.quotation.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
class QuotationControllerIntegrationTest {

    private static HttpServer alphaVantageStub;
    private static String alphaVantageBaseUrl;
    private static final AtomicReference<String> requestedPath = new AtomicReference<>();

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @AfterAll
    static void stopStub() {
        if (alphaVantageStub != null) {
            alphaVantageStub.stop(0);
        }
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        startStub();
        registry.add("alphavantage.base-url", () -> alphaVantageBaseUrl);
    }

    @Test
    void shouldImportQuotationsAndExposeThemAsJson() {
        requestedPath.set(null);

        ResponseEntity<Void> importResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/v1/quotation/import",
                Map.of("code", "VT"),
                Void.class
        );

        assertEquals(HttpStatus.NO_CONTENT, importResponse.getStatusCode());
        assertNotNull(requestedPath.get());
        assertTrue(requestedPath.get().contains("function=TIME_SERIES_DAILY"));
        assertTrue(requestedPath.get().contains("symbol=VT"));
        assertTrue(requestedPath.get().contains("outputsize=full"));
        assertTrue(requestedPath.get().contains("apikey=test-api-key"));

        ResponseEntity<String> quotationsResponse = restTemplate.getForEntity(
                "http://localhost:" + port + "/v1/quotation?code=VT",
                String.class
        );

        assertEquals(HttpStatus.OK, quotationsResponse.getStatusCode());
        assertNotNull(quotationsResponse.getBody());
        assertTrue(quotationsResponse.getBody().startsWith("["));
        assertTrue(quotationsResponse.getBody().contains("\"code\":\"VT\""));
        assertTrue(quotationsResponse.getBody().contains("\"exchangeCode\":\"NYSE\""));
        assertTrue(quotationsResponse.getBody().contains("\"currencyCode\":\"USD\""));
        assertTrue(quotationsResponse.getBody().contains("\"date\":\"2024-01-02\""));
        assertTrue(quotationsResponse.getBody().contains("\"closePrice\":101.250000000000"));
        assertTrue(quotationsResponse.getBody().contains("\"date\":\"2024-01-04\""));
        assertTrue(quotationsResponse.getBody().contains("\"closePrice\":103.500000000000"));
    }

    private static void handleRequest(HttpExchange exchange) throws IOException {
        requestedPath.set(exchange.getRequestURI().toString());
        String response = "{"
                + "\"Meta Data\":{"
                + "\"1. Information\":\"Daily Prices\","
                + "\"2. Symbol\":\"VT\""
                + "},"
                + "\"Time Series (Daily)\":{"
                + "\"2024-01-04\":{"
                + "\"1. open\":\"103.0000\","
                + "\"2. high\":\"104.0000\","
                + "\"3. low\":\"102.0000\","
                + "\"4. close\":\"103.500000000000\","
                + "\"5. volume\":\"1000\""
                + "},"
                + "\"2024-01-03\":{"
                + "\"1. open\":\"102.0000\","
                + "\"2. high\":\"103.0000\","
                + "\"3. low\":\"101.0000\","
                + "\"4. close\":\"102.000000000000\","
                + "\"5. volume\":\"900\""
                + "},"
                + "\"2024-01-02\":{"
                + "\"1. open\":\"101.0000\","
                + "\"2. high\":\"102.0000\","
                + "\"3. low\":\"100.0000\","
                + "\"4. close\":\"101.250000000000\","
                + "\"5. volume\":\"800\""
                + "}"
                + "}"
                + "}";
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, responseBytes.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(responseBytes);
        }
    }

    private static void startStub() {
        if (alphaVantageStub != null) {
            return;
        }
        try {
            alphaVantageStub = HttpServer.create(new InetSocketAddress(0), 0);
            alphaVantageStub.createContext("/query", QuotationControllerIntegrationTest::handleRequest);
            alphaVantageStub.start();
            alphaVantageBaseUrl = "http://localhost:" + alphaVantageStub.getAddress().getPort() + "/query";
        } catch (IOException e) {
            throw new IllegalStateException("Failed to start Alpha Vantage stub server", e);
        }
    }
}
