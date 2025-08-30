package piotrholda.portfoliomanager.corporateaction;

import lombok.Data;
import piotrholda.portfoliomanager.Ticker;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class Split implements CorporateAction {
    private Ticker ticker;
    private LocalDate date;
    private BigDecimal ratio;

    @Override
    public CorporateActionType getType() {
        return CorporateActionType.SPLIT;
    }

    @Override
    public BigDecimal getAmount() {
        return null;
    }

    @Override
    public String getCurrency() {
        return null;
    }

    @Override
    public void accept(CorporateActionVisitor visitor) {
        visitor.visit(this);
    }
}
