
package strategy;

import java.util.List;

public class ResolutionPlan {

    private final String title;
    private final String priority;
    private final String reason;
    private final List<String> actions;

    public ResolutionPlan(String title, String priority,
                          String reason, List<String> actions) {
        this.title = title;
        this.priority = priority;
        this.reason = reason;
        this.actions = List.copyOf(actions);
    }

    public String getTitle() {
        return title;
    }

    public String getPriority() {
        return priority;
    }

    public String getReason() {
        return reason;
    }

    public List<String> getActions() {
        return actions;
    }
}
