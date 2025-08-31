package piotrholda.portfoliomanager.corporateaction.out.http;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import piotrholda.portfoliomanager.Ticker;
import piotrholda.portfoliomanager.corporateaction.CorporateAction;
import piotrholda.portfoliomanager.corporateaction.FetchCorporateActions;
import piotrholda.portfoliomanager.stockapi.client.api.DefaultApi;
import piotrholda.portfoliomanager.stockapi.client.invoker.ApiClient;
import piotrholda.portfoliomanager.stockapi.client.model.Dividend;
import piotrholda.portfoliomanager.stockapi.client.model.Split;
import piotrholda.portfoliomanager.stockapi.client.model.StockDataResponse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
class CorporateActionsApiClient implements FetchCorporateActions {

    private final DefaultApi stockApi;

    CorporateActionsApiClient(@Value("${stock-api.base-path}") String stockApiBasePath) {
        ApiClient client = new ApiClient();
        client.setBasePath(stockApiBasePath);
        this.stockApi = new DefaultApi(client);
    }

    @Override
    public Collection<CorporateAction> get(String code) {
        StockDataResponse stockData = getStockData(code);
        List<CorporateAction> corporateActions = new ArrayList<>();
        Ticker ticker = Ticker.builder().code(code).exchangeCode("NYSE").currencyCode("USD").build();
        List<Dividend> dividends = stockData.getDividends();
        for (Dividend dividend : dividends) {
            corporateActions.add(DividendMapper.toDomain(ticker, dividend));
        }
        List<Split> splits = stockData.getSplits();
        for (Split split : splits) {
            corporateActions.add(SplitMapper.toDomain(ticker, split));
        }
        return corporateActions;
    }

    public StockDataResponse getStockData(String ticker) {
        try {
            // Call the generated method - it's fully typed!
            return this.stockApi.getStockDataApiStockDataGet(ticker);
        } catch (Exception e) {
            // The generated client throws an ApiException for non-2xx responses
            // Handle exceptions appropriately (e.g., log them, throw a custom exception)
            throw new RuntimeException("Failed to fetch stock data for ticker: " + ticker, e);
        }
    }
}
