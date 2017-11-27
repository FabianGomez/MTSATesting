package MTSSynthesis.controller.model;

import MTSTools.ac.ic.doc.mtstools.model.LTS;
import MTSSynthesis.controller.game.model.GameSolver;
import MTSSynthesis.controller.game.model.LabelledGameSolver;
import MTSSynthesis.controller.game.model.OppositeSafeGameSolver;
import MTSSynthesis.controller.model.gr.GRControllerGoal;

public class OppositeSafeControlProblem extends
		StateSpaceCuttingControlProblem<Long, String> {

	protected boolean relaxAllControllables;
	protected boolean relaxOnAssumptions;
	protected boolean relaxSelfLoops;
	
	public OppositeSafeControlProblem(LTS<Long, String> env,
			GRControllerGoal<String> grControllerGoal, Long trapState, boolean relaxAllControllables, boolean relaxOnAssumptions, boolean relaxSelfLoops) {
		super(env, grControllerGoal, trapState);
		this.relaxAllControllables	= relaxAllControllables;
		this.relaxOnAssumptions = relaxOnAssumptions;
		this.relaxSelfLoops		= relaxSelfLoops;
	}

	@Override
	protected LabelledGameSolver<Long, String, Integer> buildGameSolver() {
		if(gameSolver == null)
			gameSolver = new OppositeSafeGameSolver(environment,
				grControllerGoal.getControllableActions(), buildGuarantees(), buildAssumptions(), relaxAllControllables, relaxOnAssumptions, relaxSelfLoops);
		return gameSolver;
	}

}
