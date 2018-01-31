package MTSSynthesis.controller.game.gr.opportunist;

import MTSSynthesis.controller.game.gr.StrategyState;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ReachabilityGoalRank<S> {

    private Map<S,ReachabilityGoal> ranking;
    private Integer infinityGoal;
    private Integer infinityPath;


    public ReachabilityGoalRank(Integer infinityGoal, Integer infinityPath){
        ranking = new HashMap<>();
        this.infinityGoal = infinityGoal;
        this.infinityPath = infinityPath;
    }

    public boolean isDefined(S state){
        return ranking.containsKey(state);
    }
    public ReachabilityGoal getValue(S state){
        return ranking.get(state);
    }
    public Integer getGoal(S state){
        return ranking.get(state).getGoal();
    }
    public Integer getPath(S state){
        return ranking.get(state).getPath();
    }
    public void setState(S state, Integer goal, Integer path){
        ranking.put(state, new ReachabilityGoal(goal, path));
    }

    public ReachabilityGoal getMinimum(Set<S> states){
        ReachabilityGoal fromSuccessors = new ReachabilityGoal(infinityGoal, infinityPath);
        for( S succ : states){
            if(isDefined(succ) && getGoal(succ) < fromSuccessors.getGoal())
                fromSuccessors = getValue(succ);
            else if(isDefined(succ) && getGoal(succ).equals(fromSuccessors.getGoal()) && getPath(succ) < fromSuccessors.getPath())
                fromSuccessors = getValue(succ);
        }
        return fromSuccessors;
    }
    public ReachabilityGoal getMaximum(Set<S> states) {
        ReachabilityGoal fromSuccessors = new ReachabilityGoal(-1, infinityPath);
        for (S succ : states) {
            if (isDefined(succ) && getGoal(succ) > fromSuccessors.getGoal())
                fromSuccessors = getValue(succ);
            else if (isDefined(succ) && getGoal(succ).equals(fromSuccessors.getGoal()) && getPath(succ) < fromSuccessors.getPath())
                fromSuccessors = getValue(succ);
        }
        return fromSuccessors;
    }

    public ReachabilityGoal getInfinityValue(){
        return new ReachabilityGoal(infinityGoal, infinityPath);
    }

    public S getMinimumState(Set<S> states, boolean rankMayIncrease, int nextMemoryToConsider, OpportunistGRGameSolver solver) {
        S minState = states.iterator().next();
        ReachabilityGoal minValue = getInfinityValue();
        for (S succ : states)
            if (getGoal(succ) < minValue.getGoal()) {
                minValue = getValue(succ);
                minState = succ;
            } else if (getGoal(succ).equals(minValue.getGoal())) {

                if (getPath(succ) < minValue.getPath()) {
                    minValue = getValue(succ);
                    minState = succ;
                } else if (getPath(succ).equals(minValue.getPath())) {
                    StrategyState<S, Integer> target = new StrategyState<>(minState, nextMemoryToConsider);
                    if (solver.isBetterThan(target, new StrategyState<>(succ, nextMemoryToConsider), rankMayIncrease)) {
                        minState = succ;
                    }
                }
            }
        return minState;
    }
}
