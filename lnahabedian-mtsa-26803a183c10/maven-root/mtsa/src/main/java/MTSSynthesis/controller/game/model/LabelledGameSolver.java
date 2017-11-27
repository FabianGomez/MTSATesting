package MTSSynthesis.controller.game.model;

import MTSTools.ac.ic.doc.mtstools.model.LTS;

public interface LabelledGameSolver<S, A, M> extends GameSolver<S, M> {
	public LabelledGame<S,A> getLabelledGame();
}
