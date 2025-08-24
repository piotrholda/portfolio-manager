package piotrholda.portfoliomanager.quotation.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import piotrholda.portfoliomanager.Ticker;
import piotrholda.portfoliomanager.strategy.GetQuotations;
import piotrholda.portfoliomanager.strategy.Quotation;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class GetQuotationsUseCase implements GetQuotations {

    private final QuotationRepository repository;

    @Override
    public List<Quotation> getQuotations(Ticker ticker) {
        return repository.findAllByCodeAndExchangeCodeAndCurrencyCodeOrderByDateAsc(ticker.getCode(), ticker.getExchangeCode(),ticker.getCurrencyCode())
                .stream()
                .map(QuotationMapper::toDomain)
                .collect(Collectors.toList());
    }
}
