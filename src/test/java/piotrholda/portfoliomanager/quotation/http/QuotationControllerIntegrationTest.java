package piotrholda.portfoliomanager.quotation.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class QuotationControllerIntegrationTest {

    private static final DateTimeFormatter STOOQ_DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    private static HttpServer alphaVantageStub;
    private static HttpServer stooqStub;
    private static String alphaVantageBaseUrl;
    private static String stooqBaseUrl;
    private static final AtomicReference<String> requestedPath = new AtomicReference<>();
    private static final AtomicReference<String> requestedAuthority = new AtomicReference<>();
    private static final AtomicInteger alphaVantageRequestCount = new AtomicInteger();

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @AfterAll
    static void stopStub() {
        if (alphaVantageStub != null) {
            alphaVantageStub.stop(0);
        }
        if (stooqStub != null) {
            stooqStub.stop(0);
        }
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        startStub();
        registry.add("alphavantage.base-url", () -> alphaVantageBaseUrl);
        registry.add("stooq.base-url", () -> stooqBaseUrl);
    }

    @Test
    void shouldImportQuotationsAndExposeThemAsJson() {
        requestedPath.set(null);
        requestedAuthority.set(null);
        alphaVantageRequestCount.set(0);

        ResponseEntity<Void> importResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/v1/quotation/import",
                Map.of("code", "VT", "exchangeCode", "NYSE", "currencyCode", "USD"),
                Void.class
        );

        assertEquals(HttpStatus.NO_CONTENT, importResponse.getStatusCode());
        assertEquals("alphavantage", requestedAuthority.get());
        assertNotNull(requestedPath.get());
        assertTrue(requestedPath.get().contains("function=TIME_SERIES_DAILY"));
        assertTrue(requestedPath.get().contains("symbol=VT"));
        assertTrue(requestedPath.get().contains("outputsize=compact"));
        assertTrue(requestedPath.get().contains("apikey=test-api-key"));
        assertEquals(1, alphaVantageRequestCount.get());

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

    @Test
    void shouldImportWseQuotationsFromStooq() {
        requestedPath.set(null);
        requestedAuthority.set(null);

        ResponseEntity<Void> importResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/v1/quotation/import",
                Map.of("code", "ETFBM40TR", "exchangeCode", "WSE"),
                Void.class
        );

        assertEquals(HttpStatus.NO_CONTENT, importResponse.getStatusCode());
        assertEquals("stooq", requestedAuthority.get());
        assertNotNull(requestedPath.get());
        assertTrue(requestedPath.get().contains("s=etfbm40tr.pl"));
        assertTrue(requestedPath.get().contains("d1=19000101"));
        assertTrue(requestedPath.get().contains("d2=" + LocalDate.now().format(STOOQ_DATE_FORMAT)));
        assertTrue(requestedPath.get().contains("i=d"));

        ResponseEntity<String> quotationsResponse = restTemplate.getForEntity(
                "http://localhost:" + port + "/v1/quotation?code=ETFBM40TR&exchangeCode=WSE&currencyCode=PLN",
                String.class
        );

        assertEquals(HttpStatus.OK, quotationsResponse.getStatusCode());
        assertNotNull(quotationsResponse.getBody());
        assertTrue(quotationsResponse.getBody().contains("\"code\":\"ETFBM40TR\""));
        assertTrue(quotationsResponse.getBody().contains("\"exchangeCode\":\"WSE\""));
        assertTrue(quotationsResponse.getBody().contains("\"currencyCode\":\"PLN\""));
        assertTrue(quotationsResponse.getBody().contains("\"date\":\"2024-01-02\""));
        assertTrue(quotationsResponse.getBody().contains("\"closePrice\":101.250000000000"));
        assertTrue(quotationsResponse.getBody().contains("\"date\":\"2024-01-04\""));
        assertTrue(quotationsResponse.getBody().contains("\"closePrice\":103.500000000000"));
    }

    @Test
    void shouldReturnTooManyRequestsWhenAlphaVantageRateLimitIsReached() {
        requestedPath.set(null);
        requestedAuthority.set(null);
        alphaVantageRequestCount.set(0);

        ResponseEntity<String> importResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/v1/quotation/import",
                Map.of("code", "RATE_LIMIT", "exchangeCode", "NYSE", "currencyCode", "USD"),
                String.class
        );

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, importResponse.getStatusCode());
        assertNotNull(importResponse.getBody());
        assertTrue(importResponse.getBody().contains("Alpha Vantage rate limit reached"));
    }

    @Test
    void shouldImportQuotationsFromUploadedCsv() {
        byte[] csvBytes = ("Data,Otwarcie,Najwyzszy,Najnizszy,Zamkniecie,Wolumen\n"
                + "2019-09-05,52.61,52.99,52.4,52.4,23930\n"
                + "2019-09-06,53.00,53.10,52.80,52.95,25000\n").getBytes(StandardCharsets.UTF_8);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("code", "ETFBM40TR");
        body.add("exchangeCode", "WSE");
        body.add("currencyCode", "PLN");
        body.add("file", new ByteArrayResource(csvBytes) {
            @Override
            public String getFilename() {
                return "etfbm40tr.csv";
            }
        });

        var headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        ResponseEntity<Void> importResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/v1/quotation/import/csv",
                new org.springframework.http.HttpEntity<>(body, headers),
                Void.class
        );

        assertEquals(HttpStatus.NO_CONTENT, importResponse.getStatusCode());

        ResponseEntity<String> quotationsResponse = restTemplate.getForEntity(
                "http://localhost:" + port + "/v1/quotation?code=ETFBM40TR&exchangeCode=WSE&currencyCode=PLN",
                String.class
        );

        assertEquals(HttpStatus.OK, quotationsResponse.getStatusCode());
        assertNotNull(quotationsResponse.getBody());
        assertTrue(quotationsResponse.getBody().contains("\"code\":\"ETFBM40TR\""));
        assertTrue(quotationsResponse.getBody().contains("\"exchangeCode\":\"WSE\""));
        assertTrue(quotationsResponse.getBody().contains("\"currencyCode\":\"PLN\""));
        assertTrue(quotationsResponse.getBody().contains("\"date\":\"2019-09-05\""));
        assertTrue(quotationsResponse.getBody().contains("\"closePrice\":52.400000000000"));
        assertTrue(quotationsResponse.getBody().contains("\"date\":\"2019-09-06\""));
        assertTrue(quotationsResponse.getBody().contains("\"closePrice\":52.950000000000"));
    }

    private static void handleAlphaVantageRequest(HttpExchange exchange) throws IOException {
        requestedAuthority.set("alphavantage");
        requestedPath.set(exchange.getRequestURI().toString());
        alphaVantageRequestCount.incrementAndGet();
        String response;
        if (requestedPath.get().contains("symbol=RATE_LIMIT")) {
            response = "{"
                    + "\"Information\":\"Thank you for using Alpha Vantage! Please consider spreading out your free API requests more sparingly (1 request per second). You may subscribe to any of the premium plans at https://www.alphavantage.co/premium/ to lift the free key rate limit (25 requests per day), raise the per-second burst limit, and instantly unlock all premium endpoints.\""
                    + "}";
        } else if (requestedPath.get().contains("outputsize=full")) {
            response = "{"
                    + "\"Information\":\"Thank you for using Alpha Vantage! The outputsize=full parameter value is a premium feature for the TIME_SERIES_DAILY endpoint.\""
                    + "}";
        } else {
            response = "{"
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
        }
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, responseBytes.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(responseBytes);
        }
    }

    private static void handleStooqRequest(HttpExchange exchange) throws IOException {
        requestedAuthority.set("stooq");
        requestedPath.set(exchange.getRequestURI().toString());
        String response = "Date,Open,High,Low,Close,Volume\n"
                + "2024-01-02,101.0000,102.0000,100.0000,101.250000000000,800\n"
                + "2024-01-03,102.0000,103.0000,101.0000,102.000000000000,900\n"
                + "2024-01-04,103.0000,104.0000,102.0000,103.500000000000,1000\n";
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().add("Content-Type", "text/csv");
        exchange.sendResponseHeaders(200, responseBytes.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(responseBytes);
        }
    }

    private static void startStub() {
        if (alphaVantageStub != null && stooqStub != null) {
            return;
        }
        try {
            alphaVantageStub = HttpServer.create(new InetSocketAddress(0), 0);
            alphaVantageStub.createContext("/query", QuotationControllerIntegrationTest::handleAlphaVantageRequest);
            alphaVantageStub.start();
            alphaVantageBaseUrl = "http://localhost:" + alphaVantageStub.getAddress().getPort() + "/query";

            stooqStub = HttpServer.create(new InetSocketAddress(0), 0);
            stooqStub.createContext("/q/d/l/", QuotationControllerIntegrationTest::handleStooqRequest);
            stooqStub.start();
            stooqBaseUrl = "http://localhost:" + stooqStub.getAddress().getPort() + "/q/d/l/";
        } catch (IOException e) {
            throw new IllegalStateException("Failed to start quotation stub server", e);
        }
    }
}
