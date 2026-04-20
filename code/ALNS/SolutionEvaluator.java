package test1.LRP_ALNS_Basline;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SolutionEvaluator {
    private final LRPInstance instance;
    private final DistanceCalculator distanceCalculator;
    private final double vehicleCapacityPenalty;
    private final double facilityCapacityPenalty;
    private final double missingCustomerPenalty;
    private final double duplicateCustomerPenalty;

    public SolutionEvaluator(LRPInstance instance) {
        this(instance, 1_000.0, 1_000.0, 10_000.0, 10_000.0);
    }

    public SolutionEvaluator(
            LRPInstance instance,
            double vehicleCapacityPenalty,
            double facilityCapacityPenalty,
            double missingCustomerPenalty,
            double duplicateCustomerPenalty) {
        this.instance = instance;
        this.distanceCalculator = new DistanceCalculator(instance);
        this.vehicleCapacityPenalty = vehicleCapacityPenalty;
        this.facilityCapacityPenalty = facilityCapacityPenalty;
        this.missingCustomerPenalty = missingCustomerPenalty;
        this.duplicateCustomerPenalty = duplicateCustomerPenalty;
    }

    public void evaluate(Solution solution) {
        double objective = 0.0;
        double penalty = 0.0;

        Set<Integer> openFacilities = new HashSet<>();
        Map<Integer, Double> facilityLoad = new HashMap<>();
        Map<Integer, Integer> visitCount = new HashMap<>();

        for (Route route : solution.getRoutes()) {
            if (route.isEmpty()) {
                continue;
            }

            Facility facility = instance.getFacilityById(route.getFacilityId());
            openFacilities.add(facility.getId());

            double routeDemand = route.getDemand(instance);
            facilityLoad.merge(facility.getId(), routeDemand, Double::sum);
            if (routeDemand > instance.getVehicleCapacity()) {
                penalty += vehicleCapacityPenalty * (routeDemand - instance.getVehicleCapacity());
            }

            objective += routeTravelCost(route);
            objective += instance.getVehicleFixedCost();

            for (int customerId : route.getCustomerIds()) {
                visitCount.merge(customerId, 1, Integer::sum);
            }
        }

        for (int facilityId : openFacilities) {
            Facility facility = instance.getFacilityById(facilityId);
            objective += facility.getOpenCost();
            double load = facilityLoad.getOrDefault(facilityId, 0.0);
            if (load > facility.getCapacity()) {
                penalty += facilityCapacityPenalty * (load - facility.getCapacity());
            }
        }

        for (Customer customer : instance.getCustomers()) {
            int count = visitCount.getOrDefault(customer.getId(), 0);
            if (count == 0) {
                penalty += missingCustomerPenalty;
            } else if (count > 1) {
                penalty += duplicateCustomerPenalty * (count - 1);
            }
        }

        solution.setObjectiveValue(objective);
        solution.setPenalizedObjectiveValue(objective + penalty);
    }

    public boolean isFeasible(Solution solution) {
        Set<Integer> seen = new HashSet<>();
        Map<Integer, Double> facilityLoad = new HashMap<>();

        for (Route route : solution.getRoutes()) {
            if (route.isEmpty()) {
                continue;
            }

            double routeDemand = route.getDemand(instance);
            if (routeDemand > instance.getVehicleCapacity() + 1e-9) {
                return false;
            }

            facilityLoad.merge(route.getFacilityId(), routeDemand, Double::sum);

            for (int customerId : route.getCustomerIds()) {
                if (!seen.add(customerId)) {
                    return false;
                }
            }
        }

        for (Customer customer : instance.getCustomers()) {
            if (!seen.contains(customer.getId())) {
                return false;
            }
        }

        for (Map.Entry<Integer, Double> entry : facilityLoad.entrySet()) {
            Facility facility = instance.getFacilityById(entry.getKey());
            if (entry.getValue() > facility.getCapacity() + 1e-9) {
                return false;
            }
        }

        return true;
    }

    public double routeTravelCost(Route route) {
        Facility facility = instance.getFacilityById(route.getFacilityId());
        double cost = 0.0;
        Node previous = facility;
        for (int customerId : route.getCustomerIds()) {
            Customer customer = instance.getCustomerById(customerId);
            cost += distanceCalculator.distance(previous, customer);
            previous = customer;
        }
        cost += distanceCalculator.distance(previous, facility);
        return cost * instance.getEffectiveDistanceCostFactor();
    }

    public double insertionDelta(Route route, int customerId, int position) {
        Facility facility = instance.getFacilityById(route.getFacilityId());
        Customer inserted = instance.getCustomerById(customerId);
        Node prev = position == 0 ? facility : instance.getCustomerById(route.getCustomerIds().get(position - 1));
        Node next = position == route.size() ? facility : instance.getCustomerById(route.getCustomerIds().get(position));
        double added = distanceCalculator.distance(prev, inserted) + distanceCalculator.distance(inserted, next);
        double removed = distanceCalculator.distance(prev, next);
        return (added - removed) * instance.getEffectiveDistanceCostFactor();
    }
}