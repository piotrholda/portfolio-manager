package piotrholda.portfoliomanager.corporateaction;

import lombok.Data;
import piotrholda.portfoliomanager.Ticker;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class Dividend implements CorporateAction {
    private Ticker ticker;
    private LocalDate exDividendDate;
    private LocalDate payableDate;
    private BigDecimal amount;
    private String currency;

    @Override
    public CorporateActionType getType() {
        return CorporateActionType.DIVIDEND;
    }

    @Override
    public LocalDate getDate() {
        return exDividendDate;
    }

    @Override
    public BigDecimal getRatio() {
        return getRatio();
    }

    @Override
    public void accept(CorporateActionVisitor visitor) {
        visitor.visit(this);
    }
}
