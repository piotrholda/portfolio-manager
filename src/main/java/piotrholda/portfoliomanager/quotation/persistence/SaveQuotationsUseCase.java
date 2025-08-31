package piotrholda.portfoliomanager.quotation.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import piotrholda.portfoliomanager.quotation.SaveQuotations;
import piotrholda.portfoliomanager.strategy.Quotation;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
class SaveQuotationsUseCase implements SaveQuotations {

    private final QuotationRepository repository;

    @Override
    public void save(Collection<Quotation> quotations) {
        for (Quotation quotation: quotations) {
            Optional<QuotationEntity> quotationEntity = repository.findByCodeAndExchangeCodeAndCurrencyCodeAndDate(
                    quotation.getTicker().getCode(),
                    quotation.getTicker().getExchangeCode(),
                    quotation.getTicker().getCurrencyCode(),
                    quotation.getDate()
            );
            if (quotationEntity.isEmpty()) {
                QuotationEntity entityToSave = QuotationMapper.toEntity(quotation);
                repository.save(entityToSave);
            } else {
                QuotationEntity entityToUpdate = quotationEntity.get();
                entityToUpdate.setClosePrice(quotation.getClosePrice());
                repository.save(entityToUpdate);
            }
        }
    }
}
