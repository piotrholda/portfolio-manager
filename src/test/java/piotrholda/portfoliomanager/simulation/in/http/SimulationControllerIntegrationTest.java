package piotrholda.portfoliomanager.simulation.in.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SimulationControllerIntegrationTest {

    private static HttpServer stockApiStub;
    private static String stockApiBasePath;

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
    void shouldSimulateDualEquityMomentumAndReturnCsv() {
        importQuotations("SIM_BENCH", "100", "101", "102", "103", "104", "105", "106");
        importQuotations("SIM_RISK_FREE", "100", "100.5", "101", "101.5", "102", "102.5", "103");
        importQuotations("SIM_RISK_ON", "100", "110", "120", "130", "100", "90", "80");
        importQuotations("SIM_RISK_OFF", "100", "99", "98", "97", "110", "120", "130");
        importCorporateActions("SIM_RISK_OFF");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/v1/simulation/dualEquityMomentum",
                dualEquityMomentumRequest("SIM_"),
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.valueOf("text/csv"), response.getHeaders().getContentType());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().startsWith("Date,"));
        assertTrue(response.getBody().contains("SIM_BENCH"));
        assertTrue(response.getBody().contains("SIM_RISK_FREE"));
        assertTrue(response.getBody().contains("SIM_RISK_ON"));
        assertTrue(response.getBody().contains("SIM_RISK_OFF"));
        List<Map<String, String>> csvRows = parseCsv(response.getBody());
        assertEquals(5, csvRows.size());
        assertCsvRow(csvRows.get(0), "2024-02-29", "0.00", "0.00", "0.00", "0.00", "0.00", "SIM_RISK_ON");
        assertCsvRow(csvRows.get(1), "2024-03-31", "0.98", "0.50", "8.33", "-1.02", "8.33", "");
        assertCsvRow(csvRows.get(2), "2024-04-30", "1.96", "0.99", "-16.67", "12.24", "-16.67", "");
        assertCsvRow(csvRows.get(3), "2024-05-31", "2.94", "1.49", "-25.00", "22.45", "-25.00", "SIM_RISK_OFF");
        assertCsvRow(csvRows.get(4), "2024-06-30", "3.92", "1.98", "-33.33", "165.31", "62.50", "");
    }

    private List<Map<String, String>> parseCsv(String csv) {
        String[] lines = csv.split("\n");
        String[] headers = lines[0].split(",", -1);
        List<Map<String, String>> rows = new java.util.ArrayList<>();
        for (int i = 1; i < lines.length; i++) {
            String[] values = lines[i].split(",", -1);
            Map<String, String> row = new java.util.HashMap<>();
            for (int j = 0; j < headers.length; j++) {
                row.put(headers[j], values[j]);
            }
            rows.add(row);
        }
        return rows;
    }

    private void assertCsvRow(Map<String, String> row, String date, String benchmark, String riskFree, String riskOn,
                              String riskOff, String results, String transaction) {
        assertEquals(date, row.get("Date"));
        assertEquals(benchmark, row.get("SIM_BENCH"));
        assertEquals(riskFree, row.get("SIM_RISK_FREE"));
        assertEquals(riskOn, row.get("SIM_RISK_ON"));
        assertEquals(riskOff, row.get("SIM_RISK_OFF"));
        assertEquals(results, row.get("Results"));
        assertEquals(transaction, row.get("Transaction"));
    }

    private void importQuotations(String code, String first, String second, String third, String fourth, String fifth,
                                  String sixth, String seventh) {
        byte[] csvBytes = ("Data,Otwarcie,Najwyzszy,Najnizszy,Zamkniecie,Wolumen\n"
                + "2023-12-31," + first + "," + first + "," + first + "," + first + ",1000\n"
                + "2024-01-31," + second + "," + second + "," + second + "," + second + ",1000\n"
                + "2024-02-29," + third + "," + third + "," + third + "," + third + ",1000\n"
                + "2024-03-31," + fourth + "," + fourth + "," + fourth + "," + fourth + ",1000\n"
                + "2024-04-30," + fifth + "," + fifth + "," + fifth + "," + fifth + ",1000\n"
                + "2024-05-31," + sixth + "," + sixth + "," + sixth + "," + sixth + ",1000\n"
                + "2024-06-30," + seventh + "," + seventh + "," + seventh + "," + seventh + ",1000\n")
                .getBytes(StandardCharsets.UTF_8);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("code", code);
        body.add("exchangeCode", "NYSE");
        body.add("currencyCode", "USD");
        body.add("file", new ByteArrayResource(csvBytes) {
            @Override
            public String getFilename() {
                return code.toLowerCase() + ".csv";
            }
        });

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        ResponseEntity<Void> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/v1/quotation/import/csv",
                new HttpEntity<>(body, headers),
                Void.class
        );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    private void importCorporateActions(String code) {
        ResponseEntity<Void> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/v1/corporateaction/import",
                Map.of("code", code),
                Void.class
        );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    private Map<String, Object> dualEquityMomentumRequest(String prefix) {
        return Map.of(
                "currencyCode", "USD",
                "benchmark", ticker(prefix + "BENCH"),
                "lookBackPeriod", 1,
                "riskOffLookBackPeriod", 1,
                "riskOn", List.of(ticker(prefix + "RISK_ON")),
                "riskFree", ticker(prefix + "RISK_FREE"),
                "riskOff", List.of(ticker(prefix + "RISK_OFF")),
                "skipMonths", 0
        );
    }

    private Map<String, String> ticker(String code) {
        return Map.of("code", code, "exchangeCode", "NYSE", "currencyCode", "USD");
    }

    private static void handleStockDataRequest(HttpExchange exchange) throws IOException {
        String ticker = exchange.getRequestURI().getQuery().replace("ticker=", "");
        String response = "{"
                + "\"ticker\":\"" + ticker + "\","
                + "\"dividends\":[],"
                + "\"splits\":[{"
                + "\"Date\":\"2024-06-15\","
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
            stockApiStub.createContext("/api/stock-data", SimulationControllerIntegrationTest::handleStockDataRequest);
            stockApiStub.start();
            stockApiBasePath = "http://localhost:" + stockApiStub.getAddress().getPort();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to start stock API stub server", e);
        }
    }
}
