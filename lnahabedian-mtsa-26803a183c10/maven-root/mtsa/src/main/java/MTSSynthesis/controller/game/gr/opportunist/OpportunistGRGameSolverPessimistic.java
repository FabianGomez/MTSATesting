package MTSSynthesis.controller.game.gr.opportunist;
import MTSSynthesis.controller.game.gr.*;
import java.util.*;

public class OpportunistGRGameSolverPessimistic<S> extends OpportunistGRGameSolver<S> {


    public OpportunistGRGameSolverPessimistic(OpportunistGRGame<S> game, List<GRRankSystem<S>> rankSystem) {
        super(game, rankSystem);
    }


    @Override
    protected void addControllableSuccesors(S state, StrategyState<S, Integer> source, int nextMemoryToConsider,
                                            boolean rankMayIncrease, Set<StrategyState<S, Integer>> successors) {

        if(this.getGame().getControllableSuccessors(state).size()<= 0)
            return;

        S minState = worstGoal.getMinimumState(this.getGame().getControllableSuccessors(state), rankMayIncrease, nextMemoryToConsider, this);

        if(worstGoal.isInfinite(minState))
            return;

        StrategyState<S, Integer> target = new StrategyState<>(minState, nextMemoryToConsider);
        successors.add(target);
    }

    @Override
    public List<String> getOutputSolve(){
        if (!isGameSolved())
            this.solveGame();

        List<String> outputSolve = super.getOutputSolve();
        if(worstGoal.getGoal(this.game.getInitialState()).equals(worstGoal.getInfinityValue().getGoal() -1))
            outputSolve.add("WorstGoal from initial state: Safety");
        else
            outputSolve.add("WorstGoal from initial state: " + worstGoal.getGoal(this.game.getInitialState()));

        return outputSolve;
    }

}
