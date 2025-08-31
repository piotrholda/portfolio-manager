package piotrholda.portfoliomanager.simulation.in.http;


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
import piotrholda.portfoliomanager.simulation.SimulateDualEquityMomentum;
import piotrholda.portfoliomanager.simulation.Simulation;
import piotrholda.portfoliomanager.strategy.Quotation;
import piotrholda.portfoliomanager.strategy.Transaction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static piotrholda.portfoliomanager.infrastructure.Math.OUTPUT_CONTEXT;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/simulation")
@Tag(name = "Simulation", description = "Simulation management API")
class SimulationController {

    private final SimulateDualEquityMomentum simulateDualEquityMomentum;

    @PostMapping(value = "/dualEquityMomentum", produces = "text/csv")
    @Operation(summary = "Simulate Dual EquityMomentum strategy simulation")
    public ResponseEntity<String> dualEquityMomentum(@RequestBody SimulateDualEquityMomentumRequest request) {
        Simulation simulation = simulateDualEquityMomentum.simulate(request.toParams());
        List<Quotation> results = simulation.getResults();
        Map<Ticker, List<Quotation>> quotations = simulation.getQuotations();
        List<Transaction> transactions = simulation.getTransactions();
        List<Ticker> tickers = new ArrayList<>(quotations.keySet());

        StringBuilder csvContent = new StringBuilder();

        csvContent.append("Date,");
        for (Ticker ticker : tickers) {
            csvContent.append(ticker.getCode()).append(",");
        }
        csvContent.append("Results,");
        csvContent.append("Transaction");
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
                        csvContent.append(quotationForDate.getClosePrice().setScale(OUTPUT_CONTEXT.getPrecision(), OUTPUT_CONTEXT.getRoundingMode())).append(",");
                    } else {
                        csvContent.append(",");
                    }
                }
            }
            Optional<Quotation> resultForDate = results.stream()
                    .filter(r -> r.getDate().isEqual(date))
                    .findFirst();
            if (resultForDate.isPresent()) {
                csvContent.append(resultForDate.get().getClosePrice().setScale(OUTPUT_CONTEXT.getPrecision(), OUTPUT_CONTEXT.getRoundingMode()));
            }
            csvContent.append(",");
            Optional<Transaction> transactionToPrint = findTransaction(date, transactions);
            if (transactionToPrint.isPresent()) {
                csvContent.append(transactionToPrint.get().getTicker().getCode());
            }
            csvContent.append("\n");
        }

        HttpHeaders headers = new HttpHeaders();
        // append current date and time to file name
        String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).replace(":", "-").replace(".", "_");
        String fileName = "DualEquityMomentum_" + currentDateTime + ".csv";
        headers.add("Content-Disposition", "attachment; filename=" + fileName);
        headers.add("Content-Type", "text/csv");

        return ResponseEntity.ok()
                .headers(headers)
                .body(csvContent.toString());
    }

    private Optional<Transaction> findTransaction(LocalDate date, List<Transaction> transactions) {
        return transactions.stream()
                .filter(t -> t.getDate().isEqual(date))
                .findFirst();
    }

}
