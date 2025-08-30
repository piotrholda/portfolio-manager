package piotrholda.portfoliomanager.corporateaction.out.persistence;

import lombok.*;
import piotrholda.portfoliomanager.corporateaction.CorporateActionType;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
class CorporateActionEntity {
    @Id
    private String corporateActionId;
    @Enumerated(EnumType.STRING)
    private CorporateActionType type;
    private String code;
    private String exchangeCode;
    private String currencyCode;
    private LocalDate exDividendDate;
    private LocalDate payableDate;
    private LocalDate splitDate;
    @Column(precision = 19, scale = 12)
    private BigDecimal dividendAmount;
    private String dividendCurrency;
    @Column(precision = 19, scale = 12)
    private BigDecimal splitRatio;

    @Override
    public int hashCode() {
        return 43;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CorporateActionEntity other = (CorporateActionEntity) obj;
        if (corporateActionId == null) {
            return false;
        } else {
            return corporateActionId.equals(other.corporateActionId);
        }
    }
}
