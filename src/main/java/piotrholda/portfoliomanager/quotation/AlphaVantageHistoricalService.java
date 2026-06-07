package piotrholda.portfoliomanager.quotation;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.client.RestTemplate;

import java.util.Locale;
import java.util.Objects;

@Slf4j
@Service
class AlphaVantageHistoricalService {
    private static final String API_FREQUENCY_LIMIT_MESSAGE = "API call frequency";
    private static final String PREMIUM_FEATURE_MESSAGE = "premium feature";
    private static final String FREE_PLAN_RATE_LIMIT_MESSAGE = "please consider spreading out your free api requests more sparingly";
    private static final String DAILY_LIMIT_MESSAGE = "25 requests per day";
    private static final String INFORMATION_FIELD = "\"Information\"";
    private static final String ERROR_MESSAGE_FIELD = "\"Error Message\"";

    private final AlphaVantageConfig config;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public AlphaVantageHistoricalService(AlphaVantageConfig config) {
        this.config = config;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public TimeSeriesResponse getDailyHistory(String symbol, boolean fullData) {
        String function = "TIME_SERIES_DAILY";
        return getTimeSeries(symbol, function, fullData);
    }

    public TimeSeriesResponse getDailyAdjustedHistory(String symbol, boolean fullData) {
        String function = "TIME_SERIES_DAILY_ADJUSTED";
        return getTimeSeries(symbol, function, fullData);
    }

    public TimeSeriesResponse getWeeklyHistory(String symbol) {
        String function = "TIME_SERIES_WEEKLY";
        return getTimeSeries(symbol, function, true);
    }

    public TimeSeriesResponse getMonthlyHistory(String symbol) {
        String function = "TIME_SERIES_MONTHLY";
        return getTimeSeries(symbol, function, true);
    }

    // Get historical data for a specific month
    public TimeSeriesResponse getHistoryByMonth(String symbol, String month) {
        String url = buildHistoricalUrlWithMonth(symbol, "TIME_SERIES_DAILY", month);
        return fetchTimeSeriesData(url);
    }

    private TimeSeriesResponse getTimeSeries(String symbol, String function, boolean fullData) {
        String url = buildHistoricalUrl(symbol, function, fullData);
        try {
            return fetchTimeSeriesData(url);
        } catch (PremiumFeatureException e) {
            if (!fullData) {
                throw e;
            }
            log.warn("Alpha Vantage rejected outputsize=full for symbol {} and function {}. Falling back to compact output.", symbol, function);
            return fetchTimeSeriesData(buildHistoricalUrl(symbol, function, false));
        }
    }

    private TimeSeriesResponse fetchTimeSeriesData(String url) {
        try {
            log.info("Fetching historical quotations from Alpha Vantage: {}", sanitizeUrl(url));
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                String responseBody = Objects.requireNonNullElse(response.getBody(), "");
                String normalizedResponseBody = responseBody.toLowerCase(Locale.ROOT);

                if (normalizedResponseBody.contains(API_FREQUENCY_LIMIT_MESSAGE.toLowerCase(Locale.ROOT))) {
                    throw new AlphaVantageRateLimitException("Alpha Vantage rate limit reached");
                }
                if (normalizedResponseBody.contains(FREE_PLAN_RATE_LIMIT_MESSAGE)
                        || normalizedResponseBody.contains(DAILY_LIMIT_MESSAGE)) {
                    throw new AlphaVantageRateLimitException("Alpha Vantage free plan rate limit reached");
                }
                if (normalizedResponseBody.contains(PREMIUM_FEATURE_MESSAGE)
                        && normalizedResponseBody.contains("outputsize=full")) {
                    throw new PremiumFeatureException("Alpha Vantage outputsize=full requires a premium plan");
                }
                if (responseBody.contains(ERROR_MESSAGE_FIELD)) {
                    throw new RuntimeException("Alpha Vantage returned an error payload: " + responseBody);
                }
                if (responseBody.contains(INFORMATION_FIELD) && !responseBody.contains("\"Time Series (Daily)\"")) {
                    throw new RuntimeException("Alpha Vantage returned an informational payload instead of quotations: " + responseBody);
                }

                TimeSeriesResponse parsedResponse = objectMapper.readValue(responseBody, TimeSeriesResponse.class);
                int recordCount = parsedResponse != null && parsedResponse.getDailyTimeSeries() != null
                        ? parsedResponse.getDailyTimeSeries().size()
                        : 0;
                log.info("Alpha Vantage returned {} daily quotation records", recordCount);
                return parsedResponse;
            }

        } catch (PremiumFeatureException e) {
            throw e;
        } catch (AlphaVantageRateLimitException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to fetch historical data from Alpha Vantage", e);
            throw new RuntimeException("Failed to fetch historical data", e);
        }

        return null;
    }

    private String buildHistoricalUrl(String symbol, String function, boolean fullData) {
        String outputSize = fullData ? "full" : "compact";
        return UriComponentsBuilder.fromHttpUrl(config.getBaseUrl())
                .queryParam("function", function)
                .queryParam("symbol", symbol)
                .queryParam("outputsize", outputSize)
                .queryParam("apikey", getApiKeyOrThrow())
                .build()
                .toUriString();
    }

    private String buildHistoricalUrlWithMonth(String symbol, String function, String month) {
        return UriComponentsBuilder.fromHttpUrl(config.getBaseUrl())
                .queryParam("function", function)
                .queryParam("symbol", symbol)
                .queryParam("month", month)
                .queryParam("apikey", getApiKeyOrThrow())
                .build()
                .toUriString();
    }

    private String getApiKeyOrThrow() {
        if (config.getApiKey() == null || config.getApiKey().isBlank()) {
            throw new IllegalStateException("Alpha Vantage API key is not configured. Set ALPHAVANTAGE_API_KEY.");
        }
        return config.getApiKey();
    }

    private String sanitizeUrl(String url) {
        return url.replaceAll("(apikey=)[^&]+", "$1***");
    }

    private static class PremiumFeatureException extends RuntimeException {
        private PremiumFeatureException(String message) {
            super(message);
        }
    }
}
