package MTSSynthesis.controller.game.gr.opportunist;

public class ReachabilityGoal {
    private Integer goal;
    private Integer path;

    public ReachabilityGoal(Integer goal, Integer path){
        this.setGoal(goal);
        this.setPath(path);
    }

    public Integer getGoal() {
        return goal;
    }

    public void setGoal(Integer goal) {
        this.goal = goal;
    }

    public Integer getPath() {
        return path;
    }

    public void setPath(Integer path) {
        this.path = path;
    }
    @Override
    public String toString(){
        return "<" + goal + "," + path + ">";
    }
}
