package MTSSynthesis.controller.game.gr.opportunist;
import MTSSynthesis.controller.game.gr.*;
import MTSSynthesis.controller.game.gr.perfect.PerfectInfoGRGameSolver;
import MTSSynthesis.controller.game.model.Assume;
import MTSSynthesis.controller.game.model.Guarantee;
import MTSSynthesis.controller.game.model.Rank;
import MTSSynthesis.controller.game.model.Strategy;
import MTSSynthesis.controller.model.gr.GRGoal;
import MTSTools.ac.ic.doc.commons.relations.Pair;
import org.apache.commons.lang.Validate;

import java.util.*;

public class OpportunistGRGameSolver<S> extends PerfectInfoGRGameSolver<S> {

    protected OpportunistGRGame<S> game;
    protected List<GRRankSystem<S>> rankSystem;
    protected ReachabilityGoalRank<S> worstGoal;
    protected ReachabilityGoalRank<S> bestGoal;
    private List<String> outputSolve;

    //region Auxiliary variables
    private int actualGoal = 0;
    private Set<Integer> reachableGoals;
    //endregion

    public OpportunistGRGameSolver(OpportunistGRGame<S> game, List<GRRankSystem<S>> rankSystem) {
        super(game, rankSystem.get(0));
        this.rankSystem = rankSystem;
        this.outputSolve = new LinkedList<>();
        this.game = game;
        this.worstGoal = new ReachabilityGoalRank<>(this.getGame().getGoals().size(),this.getGame().getStates().size());
        this.bestGoal = new ReachabilityGoalRank<>(this.getGame().getGoals().size(),this.getGame().getStates().size());
    }

    //region Setters-Getters
    public List<String> getOutputSolve(){
        if (!isGameSolved())
            this.solveGame();
        outputSolve.clear();
        addOutputLine("Reachable Goals from the initial state: " + this.convertListGoalsToString(this.getReachableGoalsFromTheInitialState()));
        addOutputLine("Reachable Goals: " + this.convertListGoalsToString(this.getReachableGoals()));
        return outputSolve;
    }
    private void addOutputLine(String line) {
        this.outputSolve.add(line);
    }
    @Override
    public OpportunistGRGame<S> getGame() {
        return this.game;
    }
    //endregion

    //region Overrides
    @Override
    public GRGoal<S> getGRGoal() {
        return this.game.getGoals().get(actualGoal);
    }
    @Override
    public GRRankSystem<S> getRankSystem() {
        return this.rankSystem.get(actualGoal);
    }
    @Override
    public Strategy<S, Integer> buildStrategy() {
        reachableGoals = new HashSet<>();
        if (!isGameSolved())
            this.solveGame();

        Strategy<S, Integer> result = new Strategy<>();

        Set<S> winningStates = this.getWinningStates();

        for (S state : winningStates) {

            //Take the goal with highest priority that can be solved in this moment
            for(actualGoal = 0; actualGoal < getGame().getGoals().size(); actualGoal++)
                if(isWinningByGoal(state)) {
                    reachableGoals.add(actualGoal);
                    break;
                }

            //Add the successors to that goal and the actual state
            for (int guaranteeId = 1; guaranteeId <= this.getGRGoal().getGuaranteesQuantity(); guaranteeId++) {
                StrategyState<S, Integer> source = new StrategyState<>(state, guaranteeId);

                int nextMemoryToConsider = this.getNextGuaranteeStrategy(guaranteeId, state);

                // If either a guarantee or a failure was just visited then it
                // is ok for the successor of state to have higher rank.
                boolean rankMayIncrease = this.getGRGoal().getGuarantee(guaranteeId).contains(state)
                        || this.getGRGoal().getFailures().contains(state);

                Set<StrategyState<S, Integer>> successors = new HashSet<>();

                this.addUncontrollableSuccesors(state, source, nextMemoryToConsider, rankMayIncrease, successors);

                this.addControllableSuccesors(state, source, nextMemoryToConsider, rankMayIncrease, successors);

                Validate.notEmpty(successors, "\n State:" + source + " should have at least one successor.");
                result.addSuccessors(source, successors);
            }
            System.out.println("one winning state done");
        }

        return  result;


    }
    @Override
    public void solveGame() {
        if (this.isGameSolved()) {
            return;
        }


        for (S state : this.getGame().getStates()) {
            bestGoal.setState(state,bestGoal.getInfinityValue().getGoal(),bestGoal.getInfinityValue().getPath());
            worstGoal.setState(state,worstGoal.getInfinityValue().getGoal(),worstGoal.getInfinityValue().getPath());
        }

        //Solve the game for each goal
        for(actualGoal = 0; actualGoal < getGame().getGoals().size(); actualGoal++) {
            Queue<StrategyState<S,Integer>> pending = new LinkedList<>();

            System.out.println("Logging interval: " + TIME_TO_LOG/1000 + " seconds." );

            this.initialise(pending);
            while (!pending.isEmpty()) {

                StrategyState<S, Integer> state = pending.poll();

                checkGoals(state.getState());
                // The current rank of the state s
                Rank rank = this.getRank(state);

                // If current rank is already infinity, it obviously should
                // not be increased.
                if (rank.isInfinity()) {
                    continue;
                }

                // What is the best possible ranking that s could have according to
                // it's successors?
                Rank bestRank = this.best(state);

                // The existing ranking is already higher or equal then nothing needs to be
                // done. Go to the next state in the set of pending
                if (bestRank.compareTo(rank) <= 0) {
                    continue;
                }

                // set the new ranking of the state to the computed best value
                // If the new value is infinity it can be set for all rankings.
                this.updateRank(state, bestRank);

                this.addPredecessorsTo(pending, state, bestRank);
            }
            System.out.println("One goal solved." );
        }
        gameSolved();
        System.out.println("Solved." );
    }
    @Override
    public boolean isWinning(S state) {
        if (!isGameSolved()) {
            this.solveGame();
        }

        boolean isWin = false;
        //The state is a winning state if at least one of the rankings in not infinite
        for(actualGoal = 0; actualGoal < getGame().getGoals().size(); actualGoal++)
            isWin = isWin || !this.getRankSystem().getRank(new StrategyState<>(state, 1)).isInfinity();

        return isWin;
    }
    //endregion

    //region Auxiliary Overrides (only necessary because the superclass do not use "THIS.function" to call to a function)
    @Override
    protected void updateRank(StrategyState<S, Integer> strategyState, Rank bestRank) {
        S state = strategyState.getState();

        if (bestRank.isInfinity()) {
            // Sets infinite rank for state for all guarantees.
            for (int i = 1; i <= this.getGRGoal().getGuaranteesQuantity(); i++) {
                GRRank infinityFor = GRRank
                        .getInfinityFor(this.getRankSystem().getContext(new StrategyState<>(state, i)));
                this.getRankSystem().set(new StrategyState<>(state, i), infinityFor);
            }
        } else {
            this.getRankSystem().set(strategyState, bestRank);
        }
    }
    @Override
    protected Rank getRank(StrategyState<S, Integer> strategyState) {
        return this.getRankSystem().getRank(strategyState);
    }
    @Override
    protected Rank best(StrategyState<S, Integer> strategyState) {
        // Different ranks have different infinity values.
        S state = strategyState.getState();
        Integer guarantee = strategyState.getMemory();

        GRRank bestRank = this.getBestFromSuccessors(state, guarantee);

        if (getGRGoal().getGuarantee(1).contains(state) || getGRGoal().getFailures().contains(state)) {
            GRRankContext initialGuaranteeContext = this.getRankSystem().getContext(strategyState);

            // In this case, the infinity value of bestRank and returnRank may
            // be different.
            // We set returnRank to the desired return value and return it
            // (either (0,1) or infinity)
            if (bestRank.isInfinity()) {
                // for this guaranteeId the rank is infinity
                return GRRank.getInfinityFor(initialGuaranteeContext);
            } else {
                // for this guaranteeId the rank is "zero"
                return new GRRank(initialGuaranteeContext);
            }
        } else if (getGRGoal().getAssumption(bestRank.getAssume()).contains(state)) {
            bestRank.increase();
        }

        return bestRank;
    }
    public GRRank getBestFromSuccessors(S state, Integer guarantee) {
        int nextGuarantee = this.getNextGuarantee(guarantee, state);
        if (getGame().isUncontrollable(state)) {
            // there is no assumption about the environment
            return this.getRankSystem().getMax(
                    GRUtils.getStrategyStatesFrom(nextGuarantee, this.getGame().getUncontrollableSuccessors(state)));
        } else {
            return this.getRankSystem().getMin(
                    GRUtils.getStrategyStatesFrom(nextGuarantee, this.getGame().getControllableSuccessors(state)));
        }
    }
    @Override
    protected void initialise(Queue<StrategyState<S, Integer>> pending) {
        initializeEndingStates(pending);
        initializeStates(pending);
    }
    @Override
    protected void initializeEndingStates(Queue<StrategyState<S, Integer>> pending) {
        for (S state : this.getGame().getStates()) {
            if (this.getGame().getControllableSuccessors(state).isEmpty() && this.getGame().getUncontrollableSuccessors(state).isEmpty()) {
                for (int guaranteeId = 1; guaranteeId <= this.getGRGoal().getGuaranteesQuantity(); guaranteeId++) {
                    StrategyState<S, Integer> strategyState = new StrategyState<>(state, guaranteeId);
                    setInfinity(strategyState);
                }
                bestGoal.setStateInfinite(state);
                worstGoal.setStateInfinite(state);
                StrategyState<S, Integer> firstGuaranteeRank = new StrategyState<>(state, 1);
                Rank infinity = this.getRankSystem().getRank(firstGuaranteeRank);
                this.addPredecessorsTo(pending, firstGuaranteeRank, infinity);
            }
        }
    }
    @Override
    protected void initializeStates(Queue<StrategyState<S, Integer>> pending) {
        Assume<S> firstAssumption = this.getGRGoal().getAssumption(1);
        for (S state : this.getGame().getStates()) {
            for (int i = 1; i <= this.getGRGoal().getGuaranteesQuantity(); i++) {
                if (!this.getGRGoal().getGuarantee(i).contains(state) && !this.getGRGoal().getFailures().contains(state)
                        && firstAssumption.contains(state)) {
                    pending.add(new StrategyState<>(state, i));
                }
                if(this.getGRGoal().getGuarantee(i).contains(state)){
                    initializeWorstRank(state,actualGoal);
                    initializeBestRank(state,actualGoal);
                    addIfNotIn(pending,new StrategyState<>(state, i));
                }
            }
        }
    }
    @Override
    protected void setInfinity(StrategyState<S, Integer> strategyState) {
        GRRank infinity = GRRank.getInfinityFor(this.getRankSystem().getContext(strategyState));
        this.getRankSystem().set(strategyState, infinity);
    }
    @Override
    protected void addPredecessorsTo(Queue<StrategyState<S, Integer>> pending, StrategyState<S, Integer> strategyState, Rank bestRank) {
        S state = strategyState.getState();
        int guaranteeId = strategyState.getMemory();

        Set<S> predecessors = this.getGame().getPredecessors(state);
        for (S pred : predecessors) {

            if (bestRank.isInfinity()) {
                if (this.getGame().isUncontrollable(pred)) {
                    pending.add(new StrategyState<>(pred, 1));
                } else {
                    StrategyState<S, Integer> predecessor = new StrategyState<>(pred, 1);
                    if (this.needsToBeUpdated(predecessor)) {
                        addIfNotIn(pending, predecessor);
                    }
                }
            } else {
                StrategyState<S, Integer> predecessor = new StrategyState<>(pred, guaranteeId);
                if (this.needsToBeUpdated(predecessor)) {
                    addIfNotIn(pending, predecessor);

                }
            }
        }
    }
    @Override
    protected boolean needsToBeUpdated(StrategyState<S, Integer> predecessorStrategyState) {
        Rank best = this.best(predecessorStrategyState);
        Rank rank = this.getRankSystem().getRank(predecessorStrategyState);
        return best.compareTo(rank) > 0;
    }
    @Override
    public Set<S> getWinningStates() {
        Set<S> winning = new HashSet<>();
        if (!isGameSolved()) {
            this.solveGame();
        }
        for (S state : this.getGame().getStates()) {
            if (isWinning(state)) {
                winning.add(state);
            }
        }
        return winning;
    }
    @Override
    protected void addUncontrollableSuccesors(S state, StrategyState<S, Integer> source, int nextMemoryToConsider,
                                              boolean rankMayIncrease, Set<StrategyState<S, Integer>> successors) {
        for (S succ : this.getGame().getUncontrollableSuccessors(state)) {
            Validate.isTrue(this.isBetterThan(source, new StrategyState<>(succ, nextMemoryToConsider),
                    rankMayIncrease), "State: " + succ + " must have a better rank than state: " + state);
            StrategyState<S, Integer> target = new StrategyState<>(succ, nextMemoryToConsider);
            successors.add(target);
        }
    }
    @Override
    protected void addControllableSuccesors(S state, StrategyState<S, Integer> source, int nextMemoryToConsider,
                                            boolean rankMayIncrease, Set<StrategyState<S, Integer>> successors) {
        for (S succ : this.getGame().getControllableSuccessors(state)) {
            if (this.isBetterThan(source, new StrategyState<>(succ, nextMemoryToConsider), rankMayIncrease)) {
                Validate.isTrue(this.isWinningByGoal(succ), "state: " + succ + " it's not winning.");
                StrategyState<S, Integer> target = new StrategyState<>(succ, nextMemoryToConsider);
                successors.add(target);
            }

            else if (this.getGRGoal().buildPermissiveStrategy() && this.isWinningByGoal(succ)) {
                StrategyState<S, Integer> target = new StrategyState<>(succ, nextMemoryToConsider);
                successors.add(target);
                this.getWorseRank().add(new Pair<>(source, target));
            }
        }
    }
    @Override
    public boolean isBetterThan(StrategyState<S, Integer> state, StrategyState<S, Integer> succ, boolean mayIncrease) {

        Rank succRank = this.getRankSystem().getRank(succ);

        GRRank stateRank = this.getRankSystem().getRank(state);

        boolean isInAssumption = this.getGRGoal().getAssumption(stateRank.getAssume()).contains(state.getState());

        return ((mayIncrease && !succRank.isInfinity()) ||

                (!mayIncrease &&

                        ((stateRank.compareTo(succRank) > 0)
                                || (stateRank.compareTo(succRank) == 0 && !isInAssumption))));

    }
    @Override
    protected int getNextGuaranteeStrategy(int guaranteeId, S state) {
        Guarantee<S> guarantee = this.getGRGoal().getGuarantee(guaranteeId);
        if (guarantee.contains(state)) {
            return this.increaseGuarantee(guaranteeId);
        } else {
            return guaranteeId;
        }
    }
    //@Override no overrides because it is private on the superclass
    private int increaseGuarantee(int guaranteeId) {
        return (guaranteeId % this.getGRGoal().getGuaranteesQuantity()) + 1;
    }
    //endregion

    //region Auxiliary functions
    private boolean isWinningByGoal(S state) {
        if (!isGameSolved()) {
            this.solveGame();
        }

        return !this.getRankSystem().getRank(new StrategyState<>(state, 1)).isInfinity();
    }
    private List<Integer> getReachableGoalsFromTheInitialState(){
        List<Integer> goals = new LinkedList<>();
        //Select all the goals that its ranking are not infinity for the initial state
        for(actualGoal = 0; actualGoal < getGame().getGoals().size(); actualGoal++)
            if(isWinningByGoal(game.getInitialState()))
                goals.add(actualGoal);

        return goals;

    }
    private Set<Integer> getReachableGoals(){
        return reachableGoals;
    }
    private String convertListGoalsToString(Collection<Integer> goals){
        String result = "[";
        for(Integer goal : goals) {
            if(goal != getGame().getGoals().size()-1)   //!Safety goal
                result += "LivenessGoals[" + goal + "],";

        }
        result += "]";
        result = result.replace(",]","]");
        return result;
    }
    private void initializeWorstRank(S state, Integer newValue){
        if(newValue == -1)
            return;
        if(!worstGoal.isDefined(state))
            worstGoal.setState(state,newValue,0);
        else
            if(worstGoal.getGoal(state) > newValue)
                worstGoal.setState(state, newValue,0);
    }
    private void updateWorstRank(S state, Integer newValue, Integer path){
        if(newValue == -1)
            return;

        worstGoal.setState(state, newValue, path);
    }
    private void initializeBestRank(S state, Integer newValue){
        if(!bestGoal.isDefined(state))
            bestGoal.setState(state, newValue,0);
        else
            if(bestGoal.getGoal(state) > newValue)
                bestGoal.setState(state, newValue,0);
    }
    private void updateBestRank(S state, Integer newValue, Integer path){
        if(bestGoal.isDefined(state) && bestGoal.getGoal(state) >= newValue &&  bestGoal.getPath(state) < path)
            return;

        if(bestGoal.isDefined(state) && bestGoal.getGoal(state)  < newValue)
            return;

        bestGoal.setState(state, newValue, path);
    }
    private void checkGoals(S state){
        ReachabilityGoal bestFromSuccessors;
        ReachabilityGoal worstFromSuccessors;



        if (getGame().isUncontrollable(state)) {
            bestFromSuccessors = bestGoal.getMinimum(this.getGame().getUncontrollableSuccessors(state));
            worstFromSuccessors = worstGoal.getMaximum(this.getGame().getUncontrollableSuccessors(state));

            if(bestGoal.isInfiniteByUncontrollableSuccessors(this.getGame().getUncontrollableSuccessors(state)))
                bestGoal.setStateInfinite(state);


            if(worstGoal.isInfiniteByUncontrollableSuccessors(this.getGame().getUncontrollableSuccessors(state)))
                worstGoal.setStateInfinite(state);

        } else {
            bestFromSuccessors = bestGoal.getMinimum(this.getGame().getControllableSuccessors(state));
            worstFromSuccessors = worstGoal.getMinimum(this.getGame().getControllableSuccessors(state));

            if(bestGoal.isInfiniteByControllableSuccessors(this.getGame().getUncontrollableSuccessors(state)))
                bestGoal.setStateInfinite(state);


            if(worstGoal.isInfiniteByControllableSuccessors(this.getGame().getUncontrollableSuccessors(state)))
                worstGoal.setStateInfinite(state);

        }

        updateBestRank(state ,bestFromSuccessors.getGoal(), bestFromSuccessors.getPath() + 1 );
        updateWorstRank(state ,worstFromSuccessors.getGoal(), worstFromSuccessors.getPath() + 1 );
    }
    //endregion

}
