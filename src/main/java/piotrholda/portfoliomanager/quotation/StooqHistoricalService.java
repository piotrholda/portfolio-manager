package piotrholda.portfoliomanager.quotation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Slf4j
@Service
class StooqHistoricalService {

    private static final String START_DATE = "19000101";
    private static final DateTimeFormatter STOOQ_DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    private final StooqConfig config;
    private final RestTemplate restTemplate;

    StooqHistoricalService(StooqConfig config) {
        this.config = config;
        this.restTemplate = new RestTemplate();
    }

    List<StooqDailyQuote> getDailyHistory(String symbol) {
        String url = buildDailyHistoryUrl(symbol);
        try {
            log.info("Fetching historical quotations from Stooq for symbol {}: {}", symbol, url);
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                throw new RuntimeException("Failed to fetch Stooq data");
            }
            List<StooqDailyQuote> quotations = parseCsv(response.getBody());
            log.info("Stooq returned {} daily quotation records for symbol {}", quotations.size(), symbol);
            return quotations;
        } catch (Exception e) {
            log.error("Failed to fetch historical data from Stooq for symbol {}", symbol, e);
            throw new RuntimeException("Failed to fetch historical data from Stooq", e);
        }
    }

    private String buildDailyHistoryUrl(String symbol) {
        String endDate = LocalDate.now().format(STOOQ_DATE_FORMAT);
        String apiKeyQuery = hasApiKey() ? "&apikey=" + config.getApiKey() : "";
        return String.format("%s?s=%s&d1=%s&d2=%s&i=d%s",
                config.getBaseUrl(),
                symbol.toLowerCase(Locale.ROOT),
                START_DATE,
                endDate,
                apiKeyQuery);
    }

    private List<StooqDailyQuote> parseCsv(String csv) {
        List<StooqDailyQuote> quotations = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new StringReader(csv))) {
            String line = reader.readLine();
            validateHeader(line, csv);
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] columns = line.split(",");
                if (columns.length < 5 || containsNoData(columns)) {
                    log.debug("Skipping unsupported or incomplete Stooq CSV row: {}", line);
                    continue;
                }
                quotations.add(new StooqDailyQuote(
                        LocalDate.parse(columns[0].trim()),
                        new BigDecimal(columns[4].trim())
                ));
            }
            return quotations;
        } catch (Exception e) {
            log.error("Failed to parse Stooq CSV response", e);
            throw new RuntimeException("Failed to parse Stooq CSV response", e);
        }
    }

    private void validateHeader(String header, String csv) {
        if (header == null) {
            throw new RuntimeException("Empty response from Stooq");
        }
        String normalizedHeader = header.trim().toLowerCase(Locale.ROOT);
        if (normalizedHeader.startsWith("date,") || normalizedHeader.startsWith("data,")) {
            return;
        }

        String preview = csv.lines()
                .limit(4)
                .reduce((left, right) -> left + System.lineSeparator() + right)
                .orElse(csv);

        if (preview.toLowerCase(Locale.ROOT).contains("apikey")) {
            throw new RuntimeException("Stooq requires apikey for CSV download. Response preview: " + preview);
        }

        throw new RuntimeException("Unexpected response from Stooq. Response preview: " + preview);
    }

    private boolean containsNoData(String[] columns) {
        for (String column : columns) {
            if ("N/D".equalsIgnoreCase(column.trim())) {
                return true;
            }
        }
        return false;
    }

    private boolean hasApiKey() {
        return Objects.nonNull(config.getApiKey()) && !config.getApiKey().isBlank();
    }

    static class StooqDailyQuote {
        private final LocalDate date;
        private final BigDecimal close;

        StooqDailyQuote(LocalDate date, BigDecimal close) {
            this.date = date;
            this.close = close;
        }

        LocalDate getDate() {
            return date;
        }

        BigDecimal getClose() {
            return close;
        }
    }
}
