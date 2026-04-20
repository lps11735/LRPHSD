package test1.LRP_ALNS_Basline;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Solution {
    private final List<Route> routes;
    private double objectiveValue;
    private double penalizedObjectiveValue;

    public Solution() {
        this.routes = new ArrayList<>();
        this.objectiveValue = Double.POSITIVE_INFINITY;
        this.penalizedObjectiveValue = Double.POSITIVE_INFINITY;
    }

    public Solution(Solution other) {
        this.routes = new ArrayList<>();
        for (Route route : other.routes) {
            this.routes.add(new Route(route));
        }
        this.objectiveValue = other.objectiveValue;
        this.penalizedObjectiveValue = other.penalizedObjectiveValue;
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public void addRoute(Route route) {
        routes.add(route);
    }

    public void removeEmptyRoutes() {
        routes.removeIf(Route::isEmpty);
    }

    public Set<Integer> getOpenFacilityIds() {
        Set<Integer> open = new HashSet<>();
        for (Route route : routes) {
            if (!route.isEmpty()) {
                open.add(route.getFacilityId());
            }
        }
        return open;
    }

    public int getRouteCount() {
        return routes.size();
    }

    public double getObjectiveValue() {
        return objectiveValue;
    }

    public void setObjectiveValue(double objectiveValue) {
        this.objectiveValue = objectiveValue;
    }

    public double getPenalizedObjectiveValue() {
        return penalizedObjectiveValue;
    }

    public void setPenalizedObjectiveValue(double penalizedObjectiveValue) {
        this.penalizedObjectiveValue = penalizedObjectiveValue;
    }
}
