package piotrholda.portfoliomanager.strategy.http;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;
import piotrholda.portfoliomanager.infrastructure.http.ResultData;
import piotrholda.portfoliomanager.strategy.Strategy;

import java.util.List;

@Value
public class StrategyResponse {
    @Schema(description = "Adjusted closing prices in each ticker's currency, ordered by ticker and date")
    List<ResultData.Series> quotations;
    List<ResultData.TransactionData> transactions;

    static StrategyResponse from(Strategy strategy) {
        return new StrategyResponse(ResultData.series(strategy.getQuotations()),
                ResultData.transactions(strategy.getTransactions()));
    }
}
