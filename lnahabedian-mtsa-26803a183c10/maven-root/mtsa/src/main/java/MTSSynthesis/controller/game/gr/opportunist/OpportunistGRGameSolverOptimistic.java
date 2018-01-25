package MTSSynthesis.controller.game.gr.opportunist;
import MTSSynthesis.controller.game.gr.*;
import MTSTools.ac.ic.doc.commons.relations.Pair;

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

        S minState = this.getGame().getControllableSuccessors(state).iterator().next();
        ReachabilityGoal minValue = new ReachabilityGoal(this.getGame().getGoals().size(),this.getGame().getStates().size());
        for (S succ : this.getGame().getControllableSuccessors(state))
            if (bestRank.get(succ).getGoal() < minValue.getGoal()) {
                minValue = bestRank.get(succ);
                minState = succ;
            } else if(bestRank.get(succ).getGoal().equals(minValue.getGoal())){

                if(bestRank.get(succ).getPath() < minValue.getPath()) {
                    minValue = bestRank.get(succ);
                    minState = succ;
                }else if(bestRank.get(succ).getPath().equals(minValue.getPath())) {
                    StrategyState<S, Integer> target = new StrategyState<>(minState, nextMemoryToConsider);
                    if (this.isBetterThan(target, new StrategyState<>(succ, nextMemoryToConsider), rankMayIncrease)) {
                        minState = succ;
                    }
                }
            }
        StrategyState<S, Integer> target = new StrategyState<>(minState, nextMemoryToConsider);
        successors.add(target);

    }

}
