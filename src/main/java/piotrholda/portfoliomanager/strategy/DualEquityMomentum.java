package piotrholda.portfoliomanager.strategy;

import lombok.Data;
import piotrholda.portfoliomanager.Ticker;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Data
class DualEquityMomentum implements Strategy {

    private String currencyCode;
    private Ticker benchmark;
    private int lookBackPeriod;
    private int riskOffLookBackPeriod;
    private Collection<Ticker> riskOn;
    private Ticker riskFree;
    private Collection<Ticker> riskOff;

    private Map<Ticker, List<Quotation>> quotations;

    private List<Transaction> transactions;

    @Override
    public void execute() {
        transactions = new ArrayList<>();
        LocalDate starDate = findStartDate();
        LocalDate endDate = findEndDate();
        List<LocalDate> dates = createTimeAxis(starDate, endDate);
        List<LocalDate> checkDates = findCheckDates(dates);
        for (LocalDate checkDate: checkDates) {
            LocalDate lookBackDate = checkDate.minusMonths(lookBackPeriod);
            Ticker candidate = null;
            double candidateProfitPercent = Double.NEGATIVE_INFINITY;
            for (Ticker ticker : riskOn) {
                Quotation currentQuotation = findQuotation(checkDate, dates, quotations.get(ticker));
                Quotation lookBackQuotation = findQuotation(lookBackDate, dates, quotations.get(ticker));
                double profitPercent = currentQuotation.profitPercent(lookBackQuotation);
                if (profitPercent > candidateProfitPercent) {
                    candidate = ticker;
                    candidateProfitPercent = profitPercent;
                }
            }
            Quotation currentRiskFreeQuotation = findQuotation(checkDate, dates, quotations.get(riskFree));
            Quotation lookBackRiskFreeQuotation = findQuotation(lookBackDate, dates, quotations.get(riskFree));
            double riskFreeProfitPercent = currentRiskFreeQuotation.profitPercent(lookBackRiskFreeQuotation);
            if (riskFreeProfitPercent > candidateProfitPercent) {
                candidate = riskFree;
                LocalDate riskOffLookBackDate = checkDate.minusMonths(riskOffLookBackPeriod);
                lookBackRiskFreeQuotation = findQuotation(riskOffLookBackDate, dates, quotations.get(riskFree));
                candidateProfitPercent = currentRiskFreeQuotation.profitPercent(lookBackRiskFreeQuotation);
                for (Ticker ticker : riskOff) {
                    Quotation currentQuotation = findQuotation(checkDate, dates, quotations.get(ticker));
                    Quotation lookBackQuotation = findQuotation(riskOffLookBackDate, dates, quotations.get(ticker));
                    double profitPercent = currentQuotation.profitPercent(lookBackQuotation);
                    if (profitPercent > candidateProfitPercent) {
                        candidate = ticker;
                        candidateProfitPercent = profitPercent;
                    }
                }
            }
            transactions.add(new Transaction(findNextDate(checkDate, dates), TransactionType.BUY, candidate));
        }
    }

    private Quotation findQuotation(LocalDate checkDate, List<LocalDate> dates, List<Quotation> quotations) {
        LocalDate date = findDateOrPrevious(checkDate, dates);
        return quotations.stream()
                .filter(q -> q.getDate().equals(date))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Quotation not found for date: " + date));
    }

    private LocalDate findDateOrPrevious(LocalDate checkDate, List<LocalDate> dates) {
        LocalDate candidate = null;          // best date ≤ checkDate found so far
        int lo = 0;
        int hi = dates.size() - 1;
        while (lo <= hi) {                   // classical binary-search – O(log n)
            int mid = (lo + hi) >>> 1;
            LocalDate midVal = dates.get(mid);
            if (midVal.compareTo(checkDate) <= 0) {
                candidate = midVal;          // midVal ≤ checkDate ⇒ viable result
                lo = mid + 1;                // look further to the right
            } else {
                hi = mid - 1;                // midVal > checkDate ⇒ search left side
            }
        }
        return candidate;
    }

    private LocalDate findNextDate(LocalDate checkDate, List<LocalDate> dates) {
        LocalDate candidate = null;          // best date ≥ checkDate found so far
        int lo = 0;
        int hi = dates.size() - 1;
        while (lo <= hi) {                   // classical binary-search – O(log n)
            int mid = (lo + hi) >>> 1;
            LocalDate midVal = dates.get(mid);
            if (midVal.compareTo(checkDate) > 0) {
                candidate = midVal;          // midVal ≥ checkDate ⇒ viable result
                hi = mid - 1;                // look further to the left
            } else {
                lo = mid + 1;                // midVal < checkDate ⇒ search right side
            }
        }
        return candidate;
    }

    private List<LocalDate> findCheckDates(List<LocalDate> dates) {
        // check in the last day of the month
        LocalDate checkDate = dates.get(0).plusMonths(lookBackPeriod).with(TemporalAdjusters.lastDayOfMonth());
        List<LocalDate> checkDates = new ArrayList<>();
        checkDates.add(checkDate);
        do {
            checkDate = checkDate.plusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
            if (checkDate.isAfter(dates.get(dates.size() - 1))) {
                break;
            }
            checkDates.add(checkDate);
        } while (true);
        return checkDates;
    }

    private List<LocalDate> createTimeAxis(LocalDate startDate, LocalDate endDate) {
        Set<LocalDate> dates = new TreeSet<>();
        for (List<Quotation> quotationList : quotations.values()) {
            if (!quotationList.isEmpty()) {
                LocalDate lastDate = quotationList.get(quotationList.size() - 1).getDate();
                if (!lastDate.isBefore(startDate) && !lastDate.isAfter(endDate)) {
                    dates.add(lastDate);
                }
            }
        }
        return new ArrayList<>(dates);
    }

    private LocalDate findEndDate() {
        LocalDate endDate = LocalDate.MAX;
        for (List<Quotation> quotationList : quotations.values()) {
            if (!quotationList.isEmpty()) {
                LocalDate lastDate = quotationList.get(quotationList.size() - 1).getDate();
                if (lastDate.isBefore(endDate)) {
                    endDate = lastDate;
                }
            }
        }
        return endDate;
    }

    private LocalDate findStartDate() {
        LocalDate startDate = LocalDate.MIN;
        for (List<Quotation> quotationList : quotations.values()) {
            if (!quotationList.isEmpty()) {
                LocalDate firstDate = quotationList.get(0).getDate();
                if (firstDate.isAfter(startDate)) {
                    startDate = firstDate;
                }
            }
        }
        return startDate;
    }
}
