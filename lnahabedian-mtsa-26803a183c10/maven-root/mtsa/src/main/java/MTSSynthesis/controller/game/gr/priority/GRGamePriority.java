package MTSSynthesis.controller.game.gr.priority;
import MTSSynthesis.controller.model.gr.GRGame;
import MTSSynthesis.controller.model.gr.GRGoal;
import java.util.List;
import java.util.Set;

public class GRGamePriority<S> extends GRGame<S> {

    private List<GRGoal<S>> goals;
    private S initialState;

    public List<GRGoal<S>> getGoals() {
        return goals;
    }

    public GRGamePriority(Set<S> initialStates, Set<S> states, List<GRGoal<S>> goals, S initialState) {
        super(initialStates, states, goals.get(0));
        this.goals = goals;
        this.initialState = initialState;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer()
                .append(super.toString()).append(this.getGoal());
        return sb.toString();
    }

    @Override
    public GRGoal<S> getGoal() {
        return this.getGoals().get(0);
    }


    public S getInitialState() {
        return this.initialState;
    }
}