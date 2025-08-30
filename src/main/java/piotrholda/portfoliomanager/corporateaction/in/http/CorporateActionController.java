package piotrholda.portfoliomanager.corporateaction.in.http;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import piotrholda.portfoliomanager.corporateaction.ImportCorporateActions;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/corporateaction")
@Tag(name = "Corporate Action", description = "Corporate Action management API")
class CorporateActionController {

    private final ImportCorporateActions importCorporateActions;

    @PostMapping("/import")
    @Operation(summary = "Import Corporate Actions")
    public ResponseEntity<ImportCorporateActionsResponse> importQuotation(@RequestBody ImportCorporateActionsRequest request) {
        try {
            importCorporateActions.importCorporateActions(request.getCode());
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error initiating corporate actions import: ", e);
            return ResponseEntity.status(500).body(new ImportCorporateActionsResponse("Error initiating corporate actions import: " + e.getMessage()));
        }
    }
}
