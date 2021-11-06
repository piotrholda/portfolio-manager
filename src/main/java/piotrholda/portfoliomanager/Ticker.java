package piotrholda.portfoliomanager;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Transient;
import java.util.Optional;

import static java.util.Objects.isNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Ticker {

    private String code;
    private String exchangeCode;
    private String currencyCode;

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
