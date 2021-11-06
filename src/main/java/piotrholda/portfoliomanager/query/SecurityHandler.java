package piotrholda.portfoliomanager.query;

import lombok.AllArgsConstructor;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.springframework.stereotype.Component;
import piotrholda.portfoliomanager.SecurityCreatedEvent;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class SecurityHandler {

    private final SecurityEntityRepository securityEntityRepository;
    private final QueryUpdateEmitter queryUpdateEmitter;

    @EventHandler
    void on(SecurityCreatedEvent event) {
        SecurityEntity security = SecurityEntity.of(event);
        securityEntityRepository.save(security);
        queryUpdateEmitter.emit(FindSecurityQuery.class,
                query -> query.getSecurityId().equals(event.getSecurityId()),
                security.toResponseData());
        queryUpdateEmitter.emit(FindSecuritiesQuery.class,
                query -> true,
                security.toResponseData());
    }

    @QueryHandler
    SecurityResponseData handle(FindSecurityQuery query) {
        return securityEntityRepository.findById(query.getSecurityId().toString())
                .map(SecurityEntity::toResponseData)
                .orElseThrow(() -> new InvalidParameterException(query.getSecurityId().toString()));
    }

    @QueryHandler
    List<SecurityResponseData> handle(FindSecuritiesQuery query) {
        return securityEntityRepository.findAll().stream()
                .map(SecurityEntity::toResponseData)
                .collect(Collectors.toList());
    }

}
