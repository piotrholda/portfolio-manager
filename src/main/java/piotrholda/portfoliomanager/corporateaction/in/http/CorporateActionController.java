package piotrholda.portfoliomanager.corporateaction.in.http;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import piotrholda.portfoliomanager.corporateaction.ImportCorporateActions;
import piotrholda.portfoliomanager.strategy.GetCorporateActions;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/corporateaction")
@Tag(name = "3. Corporate Action", description = "Corporate Action management API")
class CorporateActionController {

    private final ImportCorporateActions importCorporateActions;
    private final GetCorporateActions getCorporateActions;

    @GetMapping
    @Operation(summary = "Get Corporate Actions")
    public List<CorporateActionResponse> getCorporateActions(@ModelAttribute GetCorporateActionsRequest request) {
        return getCorporateActions.get(request.toTicker()).stream()
                .map(CorporateActionResponseMapper::toResponse)
                .collect(Collectors.toList());
    }

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
