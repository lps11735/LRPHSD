package test1.LRP_ALNS_Basline;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SolutionPrinter {
    public static void print(LRPInstance instance, Solution solution) {
        System.out.println("================ Best Solution ================");
        System.out.printf("Objective value: %.3f%n", solution.getObjectiveValue());
        System.out.printf("Penalized value: %.3f%n", solution.getPenalizedObjectiveValue());
        System.out.printf("Used facilities: %d%n", solution.getOpenFacilityIds().size());
        System.out.printf("Used vehicles: %d%n", solution.getRouteCount());

        List<Route> routes = new ArrayList<>(solution.getRoutes());
        routes.sort(Comparator.comparingInt(Route::getFacilityId));
        int index = 1;
        for (Route route : routes) {
            if (route.isEmpty()) {
                continue;
            }
            double demand = route.getDemand(instance);
            System.out.printf(
                    "Route %d | facility %d | demand %.3f | customers %s%n",
                    index++, route.getFacilityId(), demand, route.getCustomerIdsView());
        }
    }
}
