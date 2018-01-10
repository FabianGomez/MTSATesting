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

        S minState = this.getGame().getControllableSuccessors(state).iterator().next();
        Integer minValue = this.getGame().getGoals().size();
        for (S succ : this.getGame().getControllableSuccessors(state))
            if (worstRank.get(succ) < minValue) {
                minValue = worstRank.get(succ);
                minState = succ;
            } else if(worstRank.get(succ) == minValue){
                if (this.isBetterThan(source, new StrategyState<>(succ, nextMemoryToConsider), rankMayIncrease)) {
                    minValue = worstRank.get(succ);
                    minState = succ;
                }
            }
        StrategyState<S, Integer> target = new StrategyState<>(minState, nextMemoryToConsider);
        successors.add(target);
    }


}
