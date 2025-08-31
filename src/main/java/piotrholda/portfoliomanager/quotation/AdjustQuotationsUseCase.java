package piotrholda.portfoliomanager.quotation;

import org.springframework.stereotype.Service;
import piotrholda.portfoliomanager.corporateaction.CorporateAction;
import piotrholda.portfoliomanager.strategy.AdjustQuotations;
import piotrholda.portfoliomanager.strategy.Quotation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static piotrholda.portfoliomanager.infrastructure.Math.MATH_CONTEXT;

@Service
class AdjustQuotationsUseCase implements AdjustQuotations {

    @Override
    public List<Quotation> adjust(List<Quotation> quotations, List<CorporateAction> corporateActions) {
        List<Quotation> adjustedQuotations = quotations.stream()
                .sorted()
                .collect(Collectors.toList());
        List<CorporateAction> sortedCorporateActions = corporateActions.stream()
                .sorted()
                .collect(Collectors.toList());
        for (int i = sortedCorporateActions.size() - 1; i >= 0; i--) {
            CorporateAction corporateAction = sortedCorporateActions.get(i);
            Optional<Quotation> previousQuotation = findPreviousQuotation(corporateAction.getDate(), adjustedQuotations);
            if (previousQuotation.isEmpty()) {
                continue;
            }
            BigDecimal ratio = corporateAction.getRatio();
            if (Objects.isNull(ratio)) {
                BigDecimal previousClosePrice = previousQuotation.get().getClosePrice();
                ratio = previousClosePrice.subtract(corporateAction.getAmount()).divide(previousClosePrice, MATH_CONTEXT);
            } else {
                ratio = BigDecimal.ONE.divide(ratio, MATH_CONTEXT);
            }
            adjustedQuotations = adjustToDate(adjustedQuotations, previousQuotation.get().getDate(), ratio);
        }
        return adjustedQuotations;
    }

    private List<Quotation> adjustToDate(List<Quotation> adjustedQuotations, LocalDate dateIncluding, BigDecimal ratio) {
        return adjustedQuotations.stream()
                .map(quotation -> {
                    if (!quotation.getDate().isAfter(dateIncluding)) {
                        BigDecimal adjustedPrice = quotation.getClosePrice().multiply(ratio);
                        return new Quotation(quotation.getTicker(), quotation.getDate(), adjustedPrice);
                    } else {
                        return quotation;
                    }
                }).collect(Collectors.toList());
    }

    private Optional<Quotation> findPreviousQuotation(LocalDate date, List<Quotation> quotations) {
        return quotations.stream()
                .filter(q -> q.getDate().isBefore(date))
                .max(Quotation::compareTo);
    }
}
