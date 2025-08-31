package piotrholda.portfoliomanager.simulation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import piotrholda.portfoliomanager.strategy.DualEquityMomentumParams;
import piotrholda.portfoliomanager.strategy.ExecuteDualEquityMomentum;
import piotrholda.portfoliomanager.strategy.Strategy;

@Service
@RequiredArgsConstructor
class SimulateDualEquityMomentumUseCase implements SimulateDualEquityMomentum {

    private final ExecuteDualEquityMomentum executeDualEquityMomentum;

    @Override
    public Simulation simulate(DualEquityMomentumParams params) {
        Strategy strategy = executeDualEquityMomentum.execute(params);
        Simulation simulation = new Simulation();
        simulation.setStrategy(strategy);
        simulation.execute();
        return simulation;
    }
}
