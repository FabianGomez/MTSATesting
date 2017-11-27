package MTSSynthesis.controller.game.gr.perfect;

import java.util.Set;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

import MTSTools.ac.ic.doc.commons.relations.Pair;
import MTSTools.ac.ic.doc.mtstools.model.LTS;
import MTSTools.ac.ic.doc.mtstools.model.impl.MTSAdapter;
import MTSSynthesis.controller.game.SimpleLabelledGame;
import MTSSynthesis.controller.game.gr.GRLabelledGame;
import MTSSynthesis.controller.game.gr.GRRankSystem;
import MTSSynthesis.controller.game.gr.StrategyState;
import MTSSynthesis.controller.game.model.GameSolver;
import MTSSynthesis.controller.game.model.LabelledGame;
import MTSSynthesis.controller.game.model.LabelledGameSolver;
import MTSSynthesis.controller.game.util.GRGameBuilder;
import MTSSynthesis.controller.game.util.GameStrategyToLTSBuilder;
import MTSSynthesis.controller.game.util.GenericLTSStrategyStateToStateConverter;
import MTSSynthesis.controller.model.OppositeSafeControlProblem;
import MTSSynthesis.controller.model.PerfectInfoOppositeGRControlProblem;
import MTSSynthesis.controller.model.StateSpaceCuttingControlProblem;
import MTSSynthesis.controller.model.gr.GRControllerGoal;
import MTSSynthesis.controller.model.gr.GRGame;
import MTSSynthesis.controller.model.gr.GRGoal;

public class StateSpaceCuttingPerfectInfoOppositeGrControlProblem extends
		StateSpaceCuttingControlProblem<Long, String> {
	protected boolean runNoG;
	protected boolean runAssumptions;
	protected boolean relaxAllControllables;
	protected boolean relaxOnAssumptions;
	protected boolean relaxSelfLoops;
	protected boolean runGR;
	protected Set<Long> losingStates;

	public StateSpaceCuttingPerfectInfoOppositeGrControlProblem(LTS<Long, String> env,
			GRControllerGoal<String> grControlGoal, Long trapState, boolean runNoG, boolean runAssumptions, boolean runGR, boolean relaxAllControllables, boolean relaxOnAssumptions, boolean relaxSelfLoops) {
		super(env, grControlGoal, trapState);
		
		this.runNoG			= runNoG;
		this.runAssumptions	= runAssumptions;
		this.relaxAllControllables	= relaxAllControllables;
		this.relaxOnAssumptions = relaxOnAssumptions;
		this.relaxSelfLoops	= relaxSelfLoops;
		this.runGR			= runGR;
	}

	protected Set<Long> solveOppositeSafe(LTS<Long,String> currentEnvironment){
		OppositeSafeControlProblem safeControlProblem = new OppositeSafeControlProblem(currentEnvironment, grControllerGoal, trapState, relaxAllControllables, relaxOnAssumptions, relaxSelfLoops);
		return safeControlProblem.getWinningStates();		
	}

	
	protected Set<Long> solveAssumptions(LTS<Long,String> currentEnvironment){
		GRControllerGoal<String> assumptionsAsGoals = grControllerGoal.cloneWithAssumptionsAsGoals();
		GRGoal<Long> grGoal 		= getGRGoalFromControllerGoal(this.environment, assumptionsAsGoals);
		LabelledGame<Long,String> game = new SimpleLabelledGame<Long, String>(this.environment, controllable);
		GRRankSystem<Long> system = new GRRankSystem<Long>(game.getStates(), grGoal.getGuarantees(), grGoal.getAssumptions(), grGoal.getFailures());
		PerfectInfoOppositeGRLabelledGameSolver<Long,String> solver = new PerfectInfoOppositeGRLabelledGameSolver<Long,String>(game, system);
		solver.solveGame();
		return 	solver.getWinningStates();
	}
	
	@Override
	protected LTS<Long, String> primitiveSolve() {
			//OPPOSITE SAFE
			Set<Long> winningNoG = null;
			
			if(runNoG){
				OppositeSafeControlProblem safeControlProblem = new OppositeSafeControlProblem(environment, grControllerGoal, trapState, relaxAllControllables, relaxOnAssumptions, relaxSelfLoops);
				safeControlProblem.solve();
				winningNoG =  safeControlProblem.getWinningStates();
			}
	
			//OPPOSITE GR
			Set<Long> winningAssumptions = null;
			
			if(runAssumptions){
				PerfectInfoOppositeGRControlProblem<Long, String> oppositeGRControlProblem = new PerfectInfoOppositeGRControlProblem<Long, String>(environment, grControllerGoal,trapState);
				oppositeGRControlProblem.solve();
				winningAssumptions =  oppositeGRControlProblem.getWinningStates();			
			}
			
			losingStates = null;
			if(runAssumptions && runNoG){
				losingStates = Sets.intersection(winningNoG, winningAssumptions);
			}
			
			else if(runAssumptions)
				losingStates = Sets.difference(environment.getStates(), winningAssumptions);
			else if(runNoG)
				losingStates =  winningNoG;
			
			if(runGR){
	
				if(losingStates !=null)
					environment 		=  removeInnerStates(losingStates);
				
				if(runGR){
					this.gameSolver = buildGameSolver();
		
					gameSolver.solveGame();
				}
			}
			return environment;
	}	

	@Override
	public Set<Long> getWinningStates() {
		if(runGR){
			return super.getWinningStates();
		}else{
			return Sets.difference(this.getGameSolver().getGame().getStates(), losingStates);
		}
	}
	
	public LTS<Long, String> buildStrategy(){
		LTS<StrategyState<Long, Integer>, String> result = GameStrategyToLTSBuilder.getInstance().buildLTSFrom(environment, gameSolver.buildStrategy());		
		return new GenericLTSStrategyStateToStateConverter<Long, String, Integer>().transform(result);		
	}
	
	@Override
	protected LabelledGameSolver<Long, String, Integer> buildGameSolver() {
		// TODO Auto-generated method stub
		GRGoal<Long> grGoal 			= getGRGoalFromControllerGoal(this.environment, grControllerGoal);
		GRLabelledGame<Long,String> game 	= new GRLabelledGame<Long, String>(this.environment, grControllerGoal.getControllableActions(), grGoal);
		GRRankSystem<Long> system 		= new GRRankSystem<Long>(game.getStates(), grGoal.getGuarantees(), grGoal.getAssumptions(), grGoal.getFailures());		
		return new PerfectInfoOppositeGRLabelledGameSolver<Long,String>(game, system);
	}	
}
