package piotrholda.portfoliomanager.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.axonframework.extensions.reactor.commandhandling.gateway.ReactorCommandGateway;
import org.axonframework.extensions.reactor.queryhandling.gateway.ReactorQueryGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.springframework.web.bind.annotation.*;
import piotrholda.portfoliomanager.command.CreateSecurityCommand;
import piotrholda.portfoliomanager.query.FindSecuritiesQuery;
import piotrholda.portfoliomanager.query.FindSecurityQuery;
import piotrholda.portfoliomanager.query.SecurityResponseData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/v1/security")
@Tag(name = "Security", description = "Security management API")
public class SecurityController {

    public static final int TIMEOUT_SECONDS = 5;

    private final ReactorCommandGateway reactorCommandGateway;
    private final ReactorQueryGateway reactorQueryGateway;

    @PostMapping
    @Operation(summary = "Create a new security")
    public Mono<SecurityResponseData> create(@RequestBody SecurityRequestData securityRequestData) {
        UUID securityId = UUID.randomUUID();
        return reactorCommandGateway.send(new CreateSecurityCommand(securityId,
                        securityRequestData.getType(),
                        securityRequestData.getName(),
                        securityRequestData.getGoogleTicker()))
                .transform(objectMono -> Mono.zip(
                                objectMono
                                        .subscribeOn(Schedulers.parallel()),
                                securitySubscriptionQuery(securityId)
                                        .subscribeOn(Schedulers.parallel()))
                        .map(Tuple2::getT2));
    }

    @GetMapping
    public Mono<List<SecurityResponseData>> all() {
        return reactorQueryGateway.query(
                new FindSecuritiesQuery(), ResponseTypes.multipleInstancesOf(SecurityResponseData.class));
    }

    @GetMapping(path = "/{securityId}")
    public Mono<SecurityResponseData> security(@PathVariable UUID securityId) {
        return reactorQueryGateway.query(
                new FindSecurityQuery(securityId), ResponseTypes.instanceOf(SecurityResponseData.class));
    }

    private Mono<SecurityResponseData> securitySubscriptionQuery(UUID securityId) {
        Flux<SecurityResponseData> queryResult = reactorQueryGateway.queryUpdates(
                new FindSecurityQuery(securityId), ResponseTypes.instanceOf(SecurityResponseData.class));
        return queryResult
                .next()
                .timeout(Duration.ofSeconds(TIMEOUT_SECONDS));
    }

}
