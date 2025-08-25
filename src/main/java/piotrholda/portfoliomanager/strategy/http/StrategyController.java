package piotrholda.portfoliomanager.strategy.http;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import piotrholda.portfoliomanager.Ticker;
import piotrholda.portfoliomanager.strategy.ExecuteDualEquityMomentum;
import piotrholda.portfoliomanager.strategy.Quotation;
import piotrholda.portfoliomanager.strategy.Strategy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/strategy")
@Tag(name = "Strategy", description = "Strategy management API")
class StrategyController {

    private final ExecuteDualEquityMomentum executeDualEquityMomentum;

    @PostMapping(value = "/dualEquityMomentum", produces = "text/csv")
    @Operation(summary = "Execute Dual EquityMomentum strategy simulation")
    public ResponseEntity<String> dualEquityMomentum(@RequestBody DualEquityMomentumRequest request) {
        Strategy strategy = executeDualEquityMomentum.execute(request.toParams());
        Map<Ticker, List<Quotation>> quotations = strategy.getQuotations();
        List<Ticker> tickers = new ArrayList<>(quotations.keySet());

        StringBuilder csvContent = new StringBuilder();

        csvContent.append("Date,");
        for (Ticker ticker : tickers) {
            csvContent.append(ticker.getCode()).append(",");
        }
        csvContent.append("\n");

        List<LocalDate> dates = quotations.values().stream()
                .flatMap(List::stream)
                .map(Quotation::getDate)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        for (LocalDate date : dates) {
            csvContent.append(date).append(",");
            for (Ticker ticker : tickers) {
                if (quotations.containsKey(ticker)) {
                    List<Quotation> tickerQuotations = quotations.get(ticker);
                    Quotation quotationForDate = tickerQuotations.stream()
                            .filter(q -> q.getDate().isEqual(date))
                            .findFirst()
                            .orElse(null);
                    if (quotationForDate != null) {
                        csvContent.append(quotationForDate.getClosePrice()).append(",");
                    } else {
                        csvContent.append(",");
                    }
                }
            }
            csvContent.append("\n");
        }

        HttpHeaders headers = new HttpHeaders();
        // append current date and time to file name
        String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).replace(":", "-").replace(".", "_");
        String fileName = "DualEquityMomentum_" + currentDateTime +".csv";
        headers.add("Content-Disposition", "attachment; filename"+fileName);
        headers.add("Content-Type", "text/csv");

        return ResponseEntity.ok()
                .headers(headers)
                .body(csvContent.toString());
    }
}
