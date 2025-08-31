package piotrholda.portfoliomanager;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import javax.persistence.Transient;
import java.util.Optional;

import static java.util.Objects.isNull;

@Value
@Jacksonized
@Builder
public class Ticker {

    String code;
    String exchangeCode;
    String currencyCode;

    @Transient
    @JsonIgnore
    public String getGoogleFormattedCode() {
        if (isNull(getCode())) {
            return null;
        }
        return Optional.ofNullable(getExchangeCode())
                .map(c -> c.concat(":"))
                .orElse("")
                .concat(code);
    }
}
