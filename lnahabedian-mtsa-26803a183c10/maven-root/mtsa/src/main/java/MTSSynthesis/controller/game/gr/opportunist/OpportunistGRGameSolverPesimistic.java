package MTSSynthesis.controller.game.gr.opportunist;
import MTSSynthesis.controller.game.gr.*;

import java.util.*;

public class OpportunistGRGameSolverPesimistic<S> extends OpportunistGRGameSolver<S> {


    public OpportunistGRGameSolverPesimistic(OpportunistGRGame<S> game, List<GRRankSystem<S>> rankSystem) {
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
            if (worstRank.get(succ) < maxValue) {
                maxValue = worstRank.get(succ);
                maxState = succ;
            } else if(worstRank.get(succ) == maxValue){
                if (this.isBetterThan(source, new StrategyState<>(succ, nextMemoryToConsider), rankMayIncrease)) {
                    maxValue = worstRank.get(succ);
                    maxState = succ;
                }
            }
        StrategyState<S, Integer> target = new StrategyState<>(maxState, nextMemoryToConsider);
        successors.add(target);
    }


}
