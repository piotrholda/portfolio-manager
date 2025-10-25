package piotrholda.portfoliomanager.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import piotrholda.portfoliomanager.query.SecurityResponseData;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/v1/security")
@Tag(name = "Security", description = "Security management API")
public class SecurityController {

    private final SecurityService securityService;

    @PostMapping
    @Operation(summary = "Create a new security")
    public Mono<SecurityResponseData> create(@RequestBody SecurityRequestData securityRequestData) {
        return securityService.create(securityRequestData);
    }

    @GetMapping
    public Mono<List<SecurityResponseData>> all() {
        return securityService.findAll();
    }

    @GetMapping(path = "/{securityId}")
    public Mono<SecurityResponseData> security(@PathVariable UUID securityId) {
        return securityService.findById(securityId);
    }
}
