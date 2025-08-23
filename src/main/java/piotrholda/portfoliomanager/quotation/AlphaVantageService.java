package piotrholda.portfoliomanager.quotation;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
class AlphaVantageService {

    private final AlphaVantageConfig config;
    private final RestTemplate restTemplate;

    public AlphaVantageService(AlphaVantageConfig config) {
        this.config = config;
        this.restTemplate = new RestTemplate();
    }

    public GlobalQuote getQuote(String symbol) {
        String url = buildQuoteUrl(symbol);

        try {
            GlobalQuoteResponse response = restTemplate.getForObject(url, GlobalQuoteResponse.class);

            if (response != null && response.getGlobalQuote() != null) {
                return response.getGlobalQuote();
            } else {
                throw new RuntimeException("No quote data received for symbol: " + symbol);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch quote for " + symbol, e);
        }
    }

    private String buildQuoteUrl(String symbol) {
        return String.format("%s?function=GLOBAL_QUOTE&symbol=%s&apikey=%s",
                config.getBaseUrl(), symbol, config.getApiKey());
    }
}
