package piotrholda.portfoliomanager.strategy.http;

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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class StrategyControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldExecuteDualEquityMomentumAndReturnCsv() {
        importQuotations("STR_BENCH", "100", "101", "102", "103");
        importQuotations("STR_RISK_FREE", "100", "100.5", "101", "101.5");
        importQuotations("STR_RISK_ON", "100", "110", "120", "130");
        importQuotations("STR_RISK_OFF", "100", "99", "98", "97");

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
        assertEquals(4, csvRows.size());
        assertCsvRow(csvRows.get(0), "2024-01-31", "100.00", "100.50", "110.00", "99.00", "");
        assertCsvRow(csvRows.get(1), "2024-02-29", "101.00", "101.00", "120.00", "98.00", "");
        assertCsvRow(csvRows.get(2), "2024-03-31", "102.00", "101.50", "130.00", "97.00", "STR_RISK_ON");
        assertCsvRow(csvRows.get(3), "2024-04-30", "103.00", "102.00", "130.00", "96.00", "");
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

    private void importQuotations(String code, String first, String second, String third, String fourth) {
        byte[] csvBytes = ("Data,Otwarcie,Najwyzszy,Najnizszy,Zamkniecie,Wolumen\n"
                + "2024-01-31," + first + "," + first + "," + first + "," + first + ",1000\n"
                + "2024-02-29," + second + "," + second + "," + second + "," + second + ",1000\n"
                + "2024-03-31," + third + "," + third + "," + third + "," + third + ",1000\n"
                + "2024-04-30," + fourth + "," + fourth + "," + fourth + "," + fourth + ",1000\n")
                .getBytes(StandardCharsets.UTF_8);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("code", code);
        body.add("exchangeCode", "WSE");
        body.add("currencyCode", "PLN");
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

    private Map<String, Object> dualEquityMomentumRequest(String prefix) {
        return Map.of(
                "currencyCode", "PLN",
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
        return Map.of("code", code, "exchangeCode", "WSE", "currencyCode", "PLN");
    }
}
