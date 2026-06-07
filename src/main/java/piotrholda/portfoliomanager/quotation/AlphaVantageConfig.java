package piotrholda.portfoliomanager.quotation;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "alphavantage")
@Component
class AlphaVantageConfig {
        private String apiKey;
        private String baseUrl;
        private boolean fullHistoryEnabled;

        // Getters and setters
        public String getApiKey() { return apiKey; }
        @SuppressWarnings("unused")
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        public String getBaseUrl() { return baseUrl; }
        @SuppressWarnings("unused")
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public boolean isFullHistoryEnabled() { return fullHistoryEnabled; }
        @SuppressWarnings("unused")
        public void setFullHistoryEnabled(boolean fullHistoryEnabled) { this.fullHistoryEnabled = fullHistoryEnabled; }
}
