package MTSSynthesis.controller.model;

import MTSTools.ac.ic.doc.mtstools.model.LTS;
import MTSTools.ac.ic.doc.mtstools.model.impl.MTSAdapter;
import MTSSynthesis.controller.game.SimpleLabelledGame;
import MTSSynthesis.controller.game.gr.GRLabelledGame;
import MTSSynthesis.controller.game.gr.GRRankSystem;
import MTSSynthesis.controller.game.gr.StrategyState;
import MTSSynthesis.controller.game.gr.perfect.PerfectInfoGRGameSolver;
import MTSSynthesis.controller.game.gr.perfect.PerfectInfoOppositeGRLabelledGameSolver;
import MTSSynthesis.controller.game.gr.perfect.PerfectInfoOppositeGRGameSolver;
import MTSSynthesis.controller.game.model.GameSolver;
import MTSSynthesis.controller.game.model.LabelledGameSolver;
import MTSSynthesis.controller.game.util.GRGameBuilder;
import MTSSynthesis.controller.game.util.GameStrategyToLTSBuilder;
import MTSSynthesis.controller.game.util.GenericLTSStrategyStateToStateConverter;
import MTSSynthesis.controller.model.gr.GRControllerGoal;
import MTSSynthesis.controller.model.gr.GRGoal;
import MTSSynthesis.controller.model.gr.concurrency.GRCGame;


public class PerfectInfoOppositeGRControlProblem<S,A> extends
	StateSpaceCuttingControlProblem<S, A>{

	public PerfectInfoOppositeGRControlProblem(LTS<S,A> originalEnvironment, GRControllerGoal<A> grControllerGoal, S trapState) {
		super(originalEnvironment, grControllerGoal, trapState);
	}	
	
	@Override
	protected LTS<S,A> primitiveSolve() {
		//cut according to all the predefined game solvers
		if(gameSolver == null)
			gameSolver 	= buildGameSolver();
		gameSolver.solveGame();
		LTS<StrategyState<S, Integer>, A> result = GameStrategyToLTSBuilder
				.getInstance().buildLTSFrom(environment,
						gameSolver.buildStrategy());
		return new GenericLTSStrategyStateToStateConverter<S, A, Integer>()
				.transform(result);		
	}	
	
	@Override
	protected LabelledGameSolver<S, A, Integer> buildGameSolver() {
		// TODO Auto-generated method stub
		GRGoal<S> grGoal 		= getGRGoalFromControllerGoal(this.environment, grControllerGoal);
		GRLabelledGame<S,A> game = new GRLabelledGame<>(environment, grControllerGoal.getControllableActions(), grGoal); 
		grControllerGoal.cloneWithAssumptionsAsGoals();
		
		GRRankSystem<S> grRankSystem = new GRRankSystem<S>(
				game.getStates(), grGoal.getGuarantees(),
				grGoal.getAssumptions(), grGoal.getFailures());
		
		PerfectInfoOppositeGRLabelledGameSolver<S,A> perfectInfoGRGameSolver = new PerfectInfoOppositeGRLabelledGameSolver<S,A>(game,  grRankSystem);
		return perfectInfoGRGameSolver;
	}
}




