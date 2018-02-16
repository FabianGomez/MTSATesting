package MTSSynthesis.controller.game.gr.opportunist;
import MTSSynthesis.controller.game.gr.*;

import java.util.*;

public class OpportunistGRGameSolverOptimistic<S> extends OpportunistGRGameSolver<S> {


    public OpportunistGRGameSolverOptimistic(OpportunistGRGame<S> game, List<GRRankSystem<S>> rankSystem) {
        super(game, rankSystem);
    }


    @Override
    protected void addControllableSuccesors(S state, StrategyState<S, Integer> source, int nextMemoryToConsider,
                                            boolean rankMayIncrease, Set<StrategyState<S, Integer>> successors) {

        if(this.getGame().getControllableSuccessors(state).size()<= 0)
            return;

        S minState = bestGoal.getMinimumState(this.getGame().getControllableSuccessors(state), rankMayIncrease, nextMemoryToConsider, this);

        if(bestGoal.isInfinite(minState))
            return;

        StrategyState<S, Integer> target = new StrategyState<>(minState, nextMemoryToConsider);
        successors.add(target);

    }

    @Override
    public List<String> getOutputSolve(){
        if (!isGameSolved())
            this.solveGame();

        List<String> outputSolve = super.getOutputSolve();
        if(bestGoal.getGoal(this.game.getInitialState()).equals(bestGoal.getInfinityValue().getGoal() -1))
            outputSolve.add("BestGoal from initial state: Safety");
        else
            outputSolve.add("BestGoal from initial state: " + bestGoal.getGoal(this.game.getInitialState()));

        return outputSolve;
    }

}
