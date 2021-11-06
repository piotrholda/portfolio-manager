package piotrholda.portfoliomanager.query;


import lombok.*;
import piotrholda.portfoliomanager.SecurityCreatedEvent;
import piotrholda.portfoliomanager.SecurityType;
import piotrholda.portfoliomanager.Ticker;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.io.Serializable;

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

    static SecurityEntity of(SecurityCreatedEvent event) {
        var ticker = event.getGoogleTicker();
        if (nonNull(ticker)) {
            return SecurityEntity.builder()
                    .securityId(event.getSecurityId().toString())
                    .type(event.getType())
                    .name(event.getName())
                    .code(ticker.getCode())
                    .exchangeCode(ticker.getExchangeCode())
                    .currencyCode(ticker.getCurrencyCode())
                    .build();
        }
        return SecurityEntity.builder()
                .securityId(event.getSecurityId().toString())
                .type(event.getType())
                .name(event.getName())
                .build();
    }

    SecurityResponseData toResponseData() {
        var ticker = new Ticker(getCode(), getExchangeCode(), getCurrencyCode());
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
