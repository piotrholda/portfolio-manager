package piotrholda.portfoliomanager.quotation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
class AlphaVantageHistoricalService {

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
        return fetchTimeSeriesData(url);
    }

    private TimeSeriesResponse fetchTimeSeriesData(String url) {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                String responseBody = response.getBody();

                // Check for API limit
                if (responseBody.contains("API call frequency")) {
                    throw new RuntimeException("API call frequency limit reached");
                }

                return objectMapper.readValue(responseBody, TimeSeriesResponse.class);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch historical data", e);
        }

        return null;
    }

    private String buildHistoricalUrl(String symbol, String function, boolean fullData) {
        String outputSize = fullData ? "full" : "compact";
        return String.format("%s?function=%s&symbol=%s&outputsize=%s&apikey=%s",
                config.getBaseUrl(), function, symbol, outputSize, config.getApiKey());
    }

    private String buildHistoricalUrlWithMonth(String symbol, String function, String month) {
        return String.format("%s?function=%s&symbol=%s&month=%s&apikey=%s",
                config.getBaseUrl(), function, symbol, month, config.getApiKey());
    }
}
