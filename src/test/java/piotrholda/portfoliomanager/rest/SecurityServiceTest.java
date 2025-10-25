package piotrholda.portfoliomanager.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import piotrholda.portfoliomanager.SecurityType;
import piotrholda.portfoliomanager.Ticker;
import piotrholda.portfoliomanager.query.SecurityResponseData;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(SecurityService.class)
class SecurityServiceTest {

    @Autowired
    private SecurityService securityService;

    @Test
    void shouldCreateAndFetchSecurity() {
        SecurityRequestData request = new SecurityRequestData();
        request.setType(SecurityType.SHARE);
        request.setName("Verizon Communications Inc.");
        request.setGoogleTicker(Ticker.builder()
                .code("VZ")
                .exchangeCode("NYSE")
                .currencyCode("USD")
                .build());

        SecurityResponseData created = securityService.create(request).block();
        assertThat(created).isNotNull();
        assertThat(created.getSecurityId()).isNotBlank();
        assertThat(created.getType()).isEqualTo(SecurityType.SHARE);
        assertThat(created.getGoogleTicker().getCode()).isEqualTo("VZ");

        UUID securityId = UUID.fromString(created.getSecurityId());
        SecurityResponseData fetched = securityService.findById(securityId).block();
        assertThat(fetched).isEqualTo(created);

        List<SecurityResponseData> all = securityService.findAll().block();
        assertThat(all).containsExactly(created);
    }
}
