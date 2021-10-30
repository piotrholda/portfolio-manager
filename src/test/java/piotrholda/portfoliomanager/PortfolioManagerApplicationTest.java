package piotrholda.portfoliomanager;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class PortfolioManagerApplicationTest {

    @Test
    void applicationContextShouldStart(@Autowired ApplicationContext applicationContext) {
       Assertions.assertThat(applicationContext.getStartupDate()).isGreaterThan(0);

    }
}