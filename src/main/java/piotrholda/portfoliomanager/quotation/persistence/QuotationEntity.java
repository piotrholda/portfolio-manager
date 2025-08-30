package piotrholda.portfoliomanager.quotation.persistence;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
class QuotationEntity {

    @Id
    private String quotationId;
    private String code;
    private String exchangeCode;
    private String currencyCode;
    private LocalDate date;
    private BigDecimal closePrice;

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
        QuotationEntity other = (QuotationEntity) obj;
        if (quotationId == null) {
            return false;
        } else {
            return quotationId.equals(other.quotationId);
        }
    }
}
