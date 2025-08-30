package piotrholda.portfoliomanager.quotation.http;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import piotrholda.portfoliomanager.quotation.ImportQuotation;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/quotation")
@Tag(name = "Quotation", description = "Quotation management API")
public class QuotationController {

    private final ImportQuotation importQuotation;

    @PostMapping("/import")
    @Operation(summary = "Import quotations")
    public ResponseEntity<ImportQuotationResponse> importQuotation(@RequestBody ImportQuotationRequest request) {
        try {
            importQuotation.importQuotations(request.getCode());
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error initiating quotation import: ", e);
            return ResponseEntity.status(500).body(new ImportQuotationResponse("Error initiating quotation import: " + e.getMessage()));
        }
    }
}
