package piotrholda.portfoliomanager.quotation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import piotrholda.portfoliomanager.Ticker;
import piotrholda.portfoliomanager.strategy.GetQuotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class QuotationSourceTest {

    @Autowired
    @Qualifier("getQuotationsUseCase")
    private GetQuotations getQuotations;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM quotation_entity");
        IntStream.range(0, 31).forEach(index -> jdbcTemplate.update(
                "INSERT INTO quotation_entity (quotation_id, code, exchange_code, currency_code, date, close_price) VALUES (?, ?, ?, ?, ?, ?)",
                "quotation-" + index,
                "VT",
                "NYSE",
                "USD",
                startDate().plusDays(index),
                BigDecimal.valueOf(100 + index)
        ));
    }

    @Test
    void shouldGetQuotations() {
        // given
        Ticker ticker = Ticker.builder().code("VT").exchangeCode("NYSE").currencyCode("USD").build();

        // when
        var quotations = getQuotations.getQuotations(ticker);

        // then
        assertNotNull(quotations);
        assertFalse(quotations.isEmpty());
        assertTrue(quotations.size() > 30);
    }

    private LocalDate startDate() {
        return LocalDate.of(2024, 1, 2);
    }
}

