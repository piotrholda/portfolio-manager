package piotrholda.portfoliomanager.quotation.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

public interface QuotationRepository extends JpaRepository<QuotationEntity, String> {

    Optional<QuotationEntity> findByCodeAndExchangeCodeAndCurrencyCodeAndDate(String code, String exchangeCode, String currencyCode, LocalDate date);
    Collection<QuotationEntity> findAllByCodeAndExchangeCodeAndCurrencyCodeOrderByDateAsc(String code, String exchangeCode, String currencyCode);
}
