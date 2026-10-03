package piotrholda.portfoliomanager.simulation.in.http;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;
import piotrholda.portfoliomanager.infrastructure.http.ResultData;
import piotrholda.portfoliomanager.simulation.Simulation;

import java.util.List;

@Value
public class SimulationResponse {
    @Schema(description = "Instrument percentage changes from simulation start (0 means unchanged, 10 means +10%), ordered by ticker and date")
    List<ResultData.Series> quotations;
    @Schema(description = "Portfolio percentage changes from simulation start (0 means unchanged, 10 means +10%), ordered by date")
    List<ResultData.Point> results;
    List<ResultData.TransactionData> transactions;

    static SimulationResponse from(Simulation simulation) {
        return new SimulationResponse(ResultData.series(simulation.getQuotations()),
                ResultData.points(simulation.getResults()), ResultData.transactions(simulation.getTransactions()));
    }
}
