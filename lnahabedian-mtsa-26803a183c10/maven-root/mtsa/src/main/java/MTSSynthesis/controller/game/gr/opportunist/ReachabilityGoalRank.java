package MTSSynthesis.controller.game.gr.opportunist;

import MTSSynthesis.controller.game.gr.StrategyState;

import java.util.*;

public class ReachabilityGoalRank<S> {

    private Map<S,ReachabilityGoal> ranking;
    private Integer infinityGoal;
    private Integer infinityPath;
    private List<S> infiniteStates;

    public ReachabilityGoalRank(Integer infinityGoal, Integer infinityPath){
        ranking = new HashMap<>();
        this.infinityGoal = infinityGoal;
        this.infinityPath = infinityPath;
        this.infiniteStates = new ArrayList<S>();
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
        if(isInfinite(state))
            return;

        ranking.put(state, new ReachabilityGoal(goal, path));
    }
    public boolean isInfinite(S state){
        return infiniteStates.contains(state);
    }
    public boolean setStateInfinite(S state){
        if(isInfinite(state))
            return false;
        ranking.put(state, new ReachabilityGoal(infinityGoal, infinityPath));
        infiniteStates.add(state);
        return true;
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
            if (isDefined(succ) && getGoal(succ) > fromSuccessors.getGoal() && !isInfinite(succ))
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
    public boolean isInfiniteByUncontrollableSuccessors(Set<S> states) {
        boolean infinite = false;
        for(S succ : states)
            infinite = infinite || isInfinite(succ);

        return infinite;
    }
    public boolean isInfiniteByControllableSuccessors(Set<S> states) {
        boolean infinite = true;
        for(S succ : states)
            infinite = infinite && isInfinite(succ);

        return infinite;
    }
}
