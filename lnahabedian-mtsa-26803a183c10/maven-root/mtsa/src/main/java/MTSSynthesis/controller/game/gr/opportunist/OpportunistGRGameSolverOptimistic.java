package MTSSynthesis.controller.game.gr.opportunist;

import MTSSynthesis.controller.game.gr.GRRankSystem;
import MTSSynthesis.controller.game.gr.StrategyState;

import java.util.List;
import java.util.Set;

public class OpportunistGRGameSolverOptimistic<S> extends OpportunistGRGameSolver<S> {


    public OpportunistGRGameSolverOptimistic(OpportunistGRGame<S> game, List<GRRankSystem<S>> rankSystem) {
        super(game, rankSystem);
    }


    @Override
    protected void addControllableSuccesors(S state, StrategyState<S, Integer> source, int nextMemoryToConsider,
                                            boolean rankMayIncrease, Set<StrategyState<S, Integer>> successors) {

        if(this.getGame().getControllableSuccessors(state).size()<= 0)
            return;

        S maxState = this.getGame().getControllableSuccessors(state).iterator().next();
        Integer maxValue = this.getGame().getGoals().size();
        for (S succ : this.getGame().getControllableSuccessors(state))
            if (bestRank.get(succ) < maxValue) {
                maxValue = bestRank.get(succ);
                maxState = succ;
            } else if(bestRank.get(succ) == maxValue){
                if (this.isBetterThan(source, new StrategyState<>(succ, nextMemoryToConsider), rankMayIncrease)) {
                    maxValue = bestRank.get(succ);
                    maxState = succ;
                }
            }
        StrategyState<S, Integer> target = new StrategyState<>(maxState, nextMemoryToConsider);
        successors.add(target);

    }


}
