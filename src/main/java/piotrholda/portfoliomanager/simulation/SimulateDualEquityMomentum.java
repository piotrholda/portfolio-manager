package piotrholda.portfoliomanager.simulation;

import piotrholda.portfoliomanager.strategy.DualEquityMomentumParams;

public interface SimulateDualEquityMomentum {

    Simulation simulate(DualEquityMomentumParams params);
}
