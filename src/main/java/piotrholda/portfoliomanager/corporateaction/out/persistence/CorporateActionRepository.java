package piotrholda.portfoliomanager.corporateaction.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import piotrholda.portfoliomanager.corporateaction.CorporateActionType;

import java.time.LocalDate;
import java.util.Optional;

public interface CorporateActionRepository extends JpaRepository<CorporateActionEntity, String> {
    Optional<CorporateActionEntity> findByTypeAndCodeAndExchangeCodeAndCurrencyCodeAndExDividendDate(CorporateActionType type, String code, String exchangeCode, String currencyCode, LocalDate exDividendDate);

    Optional<CorporateActionEntity> findByTypeAndCodeAndExchangeCodeAndCurrencyCodeAndSplitDate(CorporateActionType type, String code, String exchangeCode, String currencyCode, LocalDate splitDate);

    default Optional<CorporateActionEntity> findDividend(String code, String exchangeCode, String currencyCode, LocalDate exDividendDate) {
        return findByTypeAndCodeAndExchangeCodeAndCurrencyCodeAndExDividendDate(CorporateActionType.DIVIDEND, code, exchangeCode, currencyCode, exDividendDate);
    }

    default Optional<CorporateActionEntity> findSplit(String code, String exchangeCode, String currencyCode, LocalDate splitDate) {
        return findByTypeAndCodeAndExchangeCodeAndCurrencyCodeAndSplitDate(CorporateActionType.SPLIT, code, exchangeCode, currencyCode, splitDate);
    }
}
