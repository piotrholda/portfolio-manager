package piotrholda.portfoliomanager.quotation;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "alphavantage")
@Component
class AlphaVantageConfig {
        private String apiKey;

        // Getters and setters
        public String getApiKey() { return apiKey; }
        public String getBaseUrl() {
                return "https://www.alphavantage.co/query";
        }
}
