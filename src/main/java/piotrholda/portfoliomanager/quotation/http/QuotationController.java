package piotrholda.portfoliomanager.quotation.http;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import piotrholda.portfoliomanager.quotation.AlphaVantageRateLimitException;
import piotrholda.portfoliomanager.quotation.ImportQuotationCsv;
import piotrholda.portfoliomanager.quotation.ImportQuotation;
import piotrholda.portfoliomanager.strategy.GetQuotations;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/quotation")
@Tag(name = "2. Quotation", description = "Quotation management API")
public class QuotationController {

    private final ImportQuotation importQuotation;
    private final ImportQuotationCsv importQuotationCsv;
    private final GetQuotations getQuotations;

    @GetMapping
    @Operation(summary = "Get quotations")
    public List<QuotationResponse> getQuotations(@ModelAttribute GetQuotationRequest request) {
        return getQuotations.getQuotations(request.toTicker()).stream()
                .map(QuotationResponseMapper::toResponse)
                .collect(Collectors.toList());
    }

    @PostMapping("/import")
    @Operation(summary = "Import quotations")
    public ResponseEntity<ImportQuotationResponse> importQuotation(@RequestBody ImportQuotationRequest request) {
        try {
            importQuotation.importQuotations(request.toTicker());
            return ResponseEntity.noContent().build();
        } catch (AlphaVantageRateLimitException e) {
            log.warn("Alpha Vantage rate limit while importing quotations: {}", e.getMessage());
            return ResponseEntity.status(429).body(new ImportQuotationResponse(
                    "Alpha Vantage rate limit reached. Wait and retry, or configure another quotation source."));
        } catch (Exception e) {
            log.error("Error initiating quotation import: ", e);
            return ResponseEntity.status(500).body(new ImportQuotationResponse("Error initiating quotation import: " + e.getMessage()));
        }
    }

    @PostMapping(value = "/import/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Import quotations from uploaded CSV")
    public ResponseEntity<ImportQuotationResponse> importQuotationCsv(@ModelAttribute ImportQuotationCsvRequest request) {
        try {
            importQuotationCsv.importQuotations(request.toImportQuotationRequest().toTicker(), request.getFile());
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error importing quotations from CSV: ", e);
            return ResponseEntity.status(500).body(new ImportQuotationResponse("Error importing quotations from CSV: " + e.getMessage()));
        }
    }
}
