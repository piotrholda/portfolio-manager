package piotrholda.portfoliomanager.quotation;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "alphavantage")
@Component
class AlphaVantageConfig {
        private String apiKey;
        private String baseUrl;

        // Getters and setters
        public String getApiKey() { return apiKey; }
        @SuppressWarnings("unused")
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        public String getBaseUrl() { return baseUrl; }
        @SuppressWarnings("unused")
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
}
