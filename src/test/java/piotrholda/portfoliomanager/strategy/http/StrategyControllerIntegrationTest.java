package piotrholda.portfoliomanager.strategy.http;

import com.sun.net.httpserver.HttpExchange;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class StrategyControllerIntegrationTest {

    private static HttpServer stockApiStub;
    private static String stockApiBasePath;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldDocumentBothResponseFormats() throws Exception {
        String specification = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/v3/api-docs"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JsonNode document = objectMapper.readTree(specification);
        for (String operation : List.of("strategy", "simulation")) {
            JsonNode formats = document.get("paths").get("/v1/" + operation + "/dualEquityMomentum")
                    .get("post").get("responses").get("200").get("content");
            String responseName = operation.equals("strategy") ? "StrategyResponse" : "SimulationResponse";
            assertEquals("#/components/schemas/" + responseName,
                    formats.get("application/json").get("schema").get("$ref").asText());
            assertEquals("string", formats.get("text/csv").get("schema").get("type").asText());
            assertTrue(document.get("components").get("schemas").has(responseName));
        }
    }

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
    void shouldExecuteDualEquityMomentumAndReturnCsv() throws Exception {
        importQuotations("STR_BENCH", "100", "101", "102", "103", "104", "105", "106");
        importQuotations("STR_RISK_FREE", "100", "100.5", "101", "101.5", "102", "102.5", "103");
        importQuotations("STR_RISK_ON", "100", "110", "120", "130", "100", "90", "80");
        importQuotations("STR_RISK_OFF", "100", "99", "98", "97", "110", "120", "130");
        importCorporateActions("STR_RISK_OFF");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/v1/strategy/dualEquityMomentum",
                dualEquityMomentumRequest("STR_"),
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.valueOf("text/csv"), response.getHeaders().getContentType());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().startsWith("Date,"));
        assertTrue(response.getBody().contains("STR_BENCH"));
        assertTrue(response.getBody().contains("STR_RISK_FREE"));
        assertTrue(response.getBody().contains("STR_RISK_ON"));
        assertTrue(response.getBody().contains("STR_RISK_OFF"));
        List<Map<String, String>> csvRows = parseCsv(response.getBody());
        assertEquals(7, csvRows.size());
        assertCsvRow(csvRows.get(0), "2023-12-31", "100.00", "100.00", "100.00", "50.00", "");
        assertCsvRow(csvRows.get(1), "2024-01-31", "101.00", "100.50", "110.00", "49.50", "");
        assertCsvRow(csvRows.get(2), "2024-02-29", "102.00", "101.00", "120.00", "49.00", "STR_RISK_ON");
        assertCsvRow(csvRows.get(3), "2024-03-31", "103.00", "101.50", "130.00", "48.50", "");
        assertCsvRow(csvRows.get(4), "2024-04-30", "104.00", "102.00", "100.00", "55.00", "");
        assertCsvRow(csvRows.get(5), "2024-05-31", "105.00", "102.50", "90.00", "60.00", "STR_RISK_OFF");
       assertCsvRow(csvRows.get(6), "2024-06-30", "106.00", "103.00", "80.00", "130.00", "");
        assertJsonAndContentNegotiation(csvRows);
    }

    private void assertJsonAndContentNegotiation(List<Map<String, String>> csvRows) throws Exception {
        String request = objectMapper.writeValueAsString(dualEquityMomentumRequest("STR_"));
        String endpoint = "/v1/strategy/dualEquityMomentum";
        for (String accept : List.of("", "*/*", "text/csv", "application/json;q=0.2, text/csv;q=0.9")) {
            MockHttpServletRequestBuilder call = post(endpoint).contentType(MediaType.APPLICATION_JSON).content(request);
            if (!accept.isEmpty()) {
                call.header(HttpHeaders.ACCEPT, accept);
            }
            String csv = mockMvc.perform(call).andExpect(status().isOk())
                    .andExpect(content().contentType("text/csv"))
                    .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                            org.hamcrest.Matchers.startsWith("attachment; filename=DualEquityMomentum_")))
                    .andReturn().getResponse().getContentAsString();
            assertEquals(csvRows, parseCsv(csv));
        }
        for (String accept : List.of("application/json", "text/csv;q=0.2, application/json;q=0.9")) {
            String json = mockMvc.perform(post(endpoint).contentType(MediaType.APPLICATION_JSON)
                            .header(HttpHeaders.ACCEPT, accept).content(request))
                    .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(header().doesNotExist(HttpHeaders.CONTENT_DISPOSITION))
                    .andReturn().getResponse().getContentAsString();
            JsonNode body = objectMapper.readTree(json);
            assertEquals(2, body.size());
            assertEquals(4, body.get("quotations").size());
            for (JsonNode series : body.get("quotations")) {
                JsonNode ticker = series.get("ticker");
                assertEquals("NYSE", ticker.get("exchangeCode").asText());
                assertEquals("USD", ticker.get("currencyCode").asText());
                assertEquals(csvRows.size(), series.get("points").size());
                for (int i = 0; i < csvRows.size(); i++) {
                    assertPoint(csvRows.get(i), ticker.get("code").asText(), series.get("points").get(i));
                }
            }
            assertEquals(2, body.get("transactions").size());
            int transactionIndex = 0;
            for (Map<String, String> row : csvRows) {
                if (!row.get("Transaction").isEmpty()) {
                    JsonNode transaction = body.get("transactions").get(transactionIndex++);
                    assertEquals(row.get("Date"), transaction.get("date").asText());
                    assertEquals("BUY", transaction.get("transactionType").asText());
                    assertEquals(row.get("Transaction"), transaction.get("ticker").get("code").asText());
                }
            }
        }
        mockMvc.perform(post(endpoint).contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_XML).content(request))
                .andExpect(status().isNotAcceptable());
    }

    private void assertPoint(Map<String, String> row, String column, JsonNode point) {
        assertEquals(row.get("Date"), point.get("date").asText());
        assertTrue(point.get("value").isNumber());
        assertEquals(new java.math.BigDecimal(row.get(column)),
                point.get("value").decimalValue().setScale(2, java.math.RoundingMode.HALF_UP));
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
                              String riskOff, String transaction) {
        assertEquals(date, row.get("Date"));
        assertEquals(benchmark, row.get("STR_BENCH"));
        assertEquals(riskFree, row.get("STR_RISK_FREE"));
        assertEquals(riskOn, row.get("STR_RISK_ON"));
        assertEquals(riskOff, row.get("STR_RISK_OFF"));
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
            stockApiStub.createContext("/api/stock-data", StrategyControllerIntegrationTest::handleStockDataRequest);
            stockApiStub.start();
            stockApiBasePath = "http://localhost:" + stockApiStub.getAddress().getPort();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to start stock API stub server", e);
        }
    }
}
