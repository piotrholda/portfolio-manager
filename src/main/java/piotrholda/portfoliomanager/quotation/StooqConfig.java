package piotrholda.portfoliomanager.quotation;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "stooq")
class StooqConfig {

    private String baseUrl = "https://stooq.pl/q/d/l/";
    private String apiKey;
}
