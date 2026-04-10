package piotrholda.portfoliomanager.rest;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import piotrholda.portfoliomanager.query.SecurityEntity;
import piotrholda.portfoliomanager.query.SecurityEntityRepository;
import piotrholda.portfoliomanager.query.SecurityResponseData;
import reactor.core.publisher.Mono;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SecurityService {

    private final SecurityEntityRepository securityEntityRepository;

    public Mono<SecurityResponseData> create(SecurityRequestData securityRequestData) {
        return Mono.fromSupplier(() -> {
            UUID securityId = UUID.randomUUID();
            SecurityEntity entity = SecurityEntity.of(securityId, securityRequestData);
            SecurityEntity saved = securityEntityRepository.save(entity);
            return saved.toResponseData();
        });
    }

    public Mono<List<SecurityResponseData>> findAll() {
        return Mono.fromSupplier(() -> securityEntityRepository.findAll().stream()
                .map(SecurityEntity::toResponseData)
                .collect(Collectors.toList()));
    }

    public Mono<SecurityResponseData> findById(UUID securityId) {
        return Mono.fromSupplier(() -> securityEntityRepository.findById(securityId.toString())
                .map(SecurityEntity::toResponseData)
                .orElseThrow(() -> new InvalidParameterException(securityId.toString())));
    }
}
