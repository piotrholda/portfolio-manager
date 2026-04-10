package piotrholda.portfoliomanager.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import piotrholda.portfoliomanager.SecurityType;
import piotrholda.portfoliomanager.Ticker;
import piotrholda.portfoliomanager.rest.SecurityRequestData;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.UUID;

import static java.util.Objects.nonNull;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityEntity implements Serializable {

    @Id
    private String securityId;
    @Enumerated(EnumType.STRING)
    private SecurityType type;
    private String name;
    private String code;
    private String exchangeCode;
    private String currencyCode;

    public static SecurityEntity of(UUID securityId, SecurityRequestData requestData) {
        Ticker ticker = requestData.getGoogleTicker();
        if (nonNull(ticker)) {
            return SecurityEntity.builder()
                    .securityId(securityId.toString())
                    .type(requestData.getType())
                    .name(requestData.getName())
                    .code(ticker.getCode())
                    .exchangeCode(ticker.getExchangeCode())
                    .currencyCode(ticker.getCurrencyCode())
                    .build();
        }
        return SecurityEntity.builder()
                .securityId(securityId.toString())
                .type(requestData.getType())
                .name(requestData.getName())
                .build();
    }

    public SecurityResponseData toResponseData() {
        Ticker ticker = Ticker.builder()
                .code(getCode())
                .exchangeCode(getExchangeCode())
                .currencyCode(getCurrencyCode())
                .build();
        return new SecurityResponseData(getSecurityId(), getType(), getName(), ticker);
    }

    @Override
    public int hashCode() {
        return 42;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SecurityEntity other = (SecurityEntity) obj;
        if (securityId == null) {
            return false;
        } else {
            return securityId.equals(other.securityId);
        }
    }
}
