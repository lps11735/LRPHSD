package test1.LRP_ALNS_Basline;

import java.util.*;

public class ALNSSolver {
    private final LRPInstance instance;
    private final SolutionEvaluator evaluator;
    private final ALNSParameters parameters;
    private final Random random;
    private final OperatorScore randomRemovalScore;
    private final OperatorScore sequenceRemovalScore;
    private final OperatorScore greedyRepairScore;
    private final OperatorScore noisyRepairScore;
    private final OperatorScore procedureScore;
    private final DistanceCalculator distanceCalculator;

    public ALNSSolver(LRPInstance instance, ALNSParameters parameters) {
        this.instance = instance;
        this.parameters = parameters;
        this.evaluator = new SolutionEvaluator(instance);
        this.random = new Random(parameters.randomSeed);
        this.randomRemovalScore = new OperatorScore(parameters.initialOperatorWeight);
        this.sequenceRemovalScore = new OperatorScore(parameters.initialOperatorWeight);
        this.greedyRepairScore = new OperatorScore(parameters.initialOperatorWeight);
        this.noisyRepairScore = new OperatorScore(parameters.initialOperatorWeight);
        this.procedureScore = new OperatorScore(parameters.initialProcedureWeight);
        this.distanceCalculator = new DistanceCalculator(instance);
    }

    public Solution solve() {
        Solution current = buildInitialSolution();
        evaluator.evaluate(current);

        Solution globalBest = new Solution(current);
        Solution bestFeasible = evaluator.isFeasible(current) ? new Solution(current) : null;

        int noImprovement = 0;
        int iteration = 0;
        double temperature = parameters.temperature;

        while (iteration < parameters.maxIterations && noImprovement <= parameters.maxNoImprovement) {
            iteration++;

            DestroyType destroyType = chooseDestroyType();
            RepairType repairType = chooseRepairType();

            RemovalResult removalResult = applyDestroy(current, destroyType);
            Solution candidate = removalResult.getPartialSolution();
            boolean procedureExecuted = applyAdaptiveFacilityProcedure(candidate, removalResult.getRemovedCustomers());
            applyRepair(candidate, removalResult.getRemovedCustomers(), repairType);

            evaluator.evaluate(candidate);

            if (candidate.getPenalizedObjectiveValue()
                    < (1.0 + parameters.localSearchThreshold) * globalBest.getPenalizedObjectiveValue()) {
                localSearch(candidate);
                evaluator.evaluate(candidate);
            }

            boolean improvedGlobal = false;
            boolean accepted = false;
            double operatorReward;
            double processReward;

            if (candidate.getPenalizedObjectiveValue() < globalBest.getPenalizedObjectiveValue()) {
                current = new Solution(candidate);
                globalBest = new Solution(candidate);
                improvedGlobal = true;
                accepted = true;
                noImprovement = 0;
                operatorReward = parameters.rewardGlobalBest;
                processReward = parameters.procedureRewardGlobalBest;
            } else if (candidate.getPenalizedObjectiveValue() < current.getPenalizedObjectiveValue()) {
                current = new Solution(candidate);
                accepted = true;
                noImprovement++;
                operatorReward = parameters.rewardCurrentBest;
                processReward = parameters.procedureRewardCurrentBest;
            } else if (accept(candidate.getPenalizedObjectiveValue(), current.getPenalizedObjectiveValue(), temperature)) {
                current = new Solution(candidate);
                accepted = true;
                noImprovement++;
                operatorReward = parameters.rewardAcceptedWorse;
                processReward = parameters.procedureRewardAcceptedWorse;
            } else {
                noImprovement++;
                operatorReward = parameters.rewardRejected;
                processReward = parameters.procedureRewardRejected;
            }

            // Maintain best feasible solution independently
            if (evaluator.isFeasible(candidate)) {
                if (bestFeasible == null || candidate.getObjectiveValue() < bestFeasible.getObjectiveValue()) {
                    bestFeasible = new Solution(candidate);
                }
            }

            updateOperatorWeights(destroyType, repairType, operatorReward);
            if (procedureExecuted) {
                procedureScore.update(parameters.reactionFactor, processReward);
            }

            temperature = Math.max(1e-6, temperature * parameters.coolingRate);

            if (iteration % 1000 == 0) {
                String feasibleInfo;
                if (bestFeasible == null) {
                    feasibleInfo = "none";
                } else {
                    feasibleInfo = String.format("%.3f", bestFeasible.getObjectiveValue());
                }

                System.out.printf(
                        "Iteration %d | current = %.3f | best = %.3f | bestFeasible = %s | accepted = %s | globalImproved = %s%n",
                        iteration,
                        current.getPenalizedObjectiveValue(),
                        globalBest.getPenalizedObjectiveValue(),
                        feasibleInfo,
                        accepted,
                        improvedGlobal);
            }
        }

        // Final decision: always prefer feasible solution
        if (bestFeasible != null) {
            evaluator.evaluate(bestFeasible);
            return bestFeasible;
        }

        // If no feasible solution was found, try one final repair
        Solution repaired = new Solution(globalBest);
        repairToFeasible(repaired);
        evaluator.evaluate(repaired);

        if (evaluator.isFeasible(repaired)) {
            return repaired;
        }

        // Last fallback
        evaluator.evaluate(globalBest);
        return globalBest;
    }

    private void repairToFeasible(Solution solution) {
        boolean changed = true;

        while (changed) {
            changed = false;

            // 1. Remove duplicates
            Set<Integer> seen = new HashSet<>();
            List<Integer> removedCustomers = new ArrayList<>();

            for (Route route : solution.getRoutes()) {
                List<Integer> toRemove = new ArrayList<>();
                for (int customerId : route.getCustomerIdsView()) {
                    if (!seen.add(customerId)) {
                        toRemove.add(customerId);
                    }
                }
                for (int customerId : toRemove) {
                    route.removeCustomer(customerId);
                    removedCustomers.add(customerId);
                    changed = true;
                }
            }

            // 2. Fix vehicle capacity violations
            for (Route route : solution.getRoutes()) {
                while (route.getDemand(instance) > instance.getVehicleCapacity() + 1e-9 && !route.isEmpty()) {
                    int worstCustomer = chooseCustomerToRemove(route);
                    route.removeCustomer(worstCustomer);
                    removedCustomers.add(worstCustomer);
                    changed = true;
                }
            }

            // 3. Fix facility capacity violations
            Map<Integer, Double> facilityLoad = new HashMap<>();
            for (Route route : solution.getRoutes()) {
                if (!route.isEmpty()) {
                    facilityLoad.merge(route.getFacilityId(), route.getDemand(instance), Double::sum);
                }
            }

            for (Integer facilityId : new ArrayList<>(facilityLoad.keySet())) {
                Facility facility = instance.getFacilityById(facilityId);

                while (facilityLoad.getOrDefault(facilityId, 0.0) > facility.getCapacity() + 1e-9) {
                    Route routeToReduce = null;
                    for (Route route : solution.getRoutes()) {
                        if (route.getFacilityId() == facilityId && !route.isEmpty()) {
                            if (routeToReduce == null || route.getDemand(instance) > routeToReduce.getDemand(instance)) {
                                routeToReduce = route;
                            }
                        }
                    }

                    if (routeToReduce == null) {
                        break;
                    }

                    int worstCustomer = chooseCustomerToRemove(routeToReduce);
                    double demand = instance.getCustomerById(worstCustomer).getDemand();
                    routeToReduce.removeCustomer(worstCustomer);
                    removedCustomers.add(worstCustomer);
                    facilityLoad.put(facilityId, facilityLoad.get(facilityId) - demand);
                    changed = true;
                }
            }

            solution.removeEmptyRoutes();

            // 4. Reinsert removed customers
            if (!removedCustomers.isEmpty()) {
                Collections.shuffle(removedCustomers, random);
                for (int customerId : removedCustomers) {
                    insertCustomerGreedy(solution, customerId, false);
                }
                solution.removeEmptyRoutes();
            }

            // 5. Add missing customers if any
            Set<Integer> served = new HashSet<>();
            for (Route route : solution.getRoutes()) {
                served.addAll(route.getCustomerIdsView());
            }

            for (Customer customer : instance.getCustomers()) {
                if (!served.contains(customer.getId())) {
                    insertCustomerGreedy(solution, customer.getId(), false);
                    changed = true;
                }
            }

            evaluator.evaluate(solution);

            // Stop early if already feasible
            if (evaluator.isFeasible(solution)) {
                return;
            }
        }
    }

    private int chooseCustomerToRemove(Route route) {
        int bestCustomerId = route.getCustomerIds().get(route.size() - 1);
        double bestDemand = -1.0;

        for (int customerId : route.getCustomerIds()) {
            double demand = instance.getCustomerById(customerId).getDemand();
            if (demand > bestDemand) {
                bestDemand = demand;
                bestCustomerId = customerId;
            }
        }
        return bestCustomerId;
    }

    private Solution buildInitialSolution() {
        Solution solution = new Solution();
        Facility seedFacility = chooseInitialFacility();
        int routeCount = (int) Math.ceil(totalDemand() / instance.getVehicleCapacity());
        routeCount = Math.max(routeCount, 1);
        int facilityRouteCap = seedFacility.getMaxVehicles() > 0 ? seedFacility.getMaxVehicles() : routeCount;
        routeCount = Math.min(routeCount, facilityRouteCap);

        for (int r = 0; r < routeCount; r++) {
            solution.addRoute(new Route(seedFacility.getId()));
        }

        List<Customer> customers = new ArrayList<>(instance.getCustomers());
        customers.sort(Comparator.comparingDouble(Customer::getDemand).reversed());
        for (Customer customer : customers) {
            insertCustomerGreedy(solution, customer.getId(), false);
        }
        solution.removeEmptyRoutes();
        evaluator.evaluate(solution);
        return solution;
    }

    private Facility chooseInitialFacility() {
        Facility best = null;
        double bestScore = Double.POSITIVE_INFINITY;
        for (Facility facility : instance.getFacilities()) {
            double weightedDistance = facility.getOpenCost();
            for (Customer customer : instance.getCustomers()) {
                weightedDistance += distanceCalculator.distance(facility, customer) * customer.getDemand();
            }
            if (weightedDistance < bestScore) {
                bestScore = weightedDistance;
                best = facility;
            }
        }
        if (best == null) {
            throw new IllegalStateException("No feasible initial facility found.");
        }
        return best;
    }

    private double totalDemand() {
        double total = 0.0;
        for (Customer customer : instance.getCustomers()) {
            total += customer.getDemand();
        }
        return total;
    }

    private DestroyType chooseDestroyType() {
        double total = randomRemovalScore.getWeight() + sequenceRemovalScore.getWeight();
        double draw = random.nextDouble() * total;
        return draw <= randomRemovalScore.getWeight() ? DestroyType.RANDOM_REMOVAL : DestroyType.SEQUENCE_REMOVAL;
    }

    private RepairType chooseRepairType() {
        double total = greedyRepairScore.getWeight() + noisyRepairScore.getWeight();
        double draw = random.nextDouble() * total;
        return draw <= greedyRepairScore.getWeight() ? RepairType.GREEDY_INSERTION : RepairType.GREEDY_INSERTION_WITH_NOISE;
    }

    private RemovalResult applyDestroy(Solution current, DestroyType destroyType) {
        Solution partial = new Solution(current);
        List<Integer> removed = new ArrayList<>();
        int betaMax = (int) Math.ceil(Math.min(Math.max(10, 0.2 * instance.getCustomerCount()), 20));
        int beta = 1 + random.nextInt(betaMax);

        if (destroyType == DestroyType.RANDOM_REMOVAL) {
            List<Integer> allCustomers = collectCustomers(partial);
            Collections.shuffle(allCustomers, random);
            int removeCount = Math.min(beta, allCustomers.size());
            for (int i = 0; i < removeCount; i++) {
                int customerId = allCustomers.get(i);
                removeCustomer(partial, customerId);
                removed.add(customerId);
            }
        } else {
            List<Route> nonEmptyRoutes = collectNonEmptyRoutes(partial);
            if (!nonEmptyRoutes.isEmpty()) {
                Route seedRoute = nonEmptyRoutes.get(random.nextInt(nonEmptyRoutes.size()));
                int start = random.nextInt(seedRoute.size());
                while (removed.size() < beta) {
                    if (seedRoute.isEmpty()) {
                        break;
                    }
                    int index = Math.min(start, seedRoute.size() - 1);
                    int customerId = seedRoute.removeCustomerAt(index);
                    removed.add(customerId);
                    if (start > 0) {
                        start--;
                    }
                    if (removed.size() >= beta) {
                        break;
                    }
                    List<Route> stillNonEmpty = collectNonEmptyRoutes(partial);
                    if (stillNonEmpty.isEmpty()) {
                        break;
                    }
                    seedRoute = stillNonEmpty.get(random.nextInt(stillNonEmpty.size()));
                    start = random.nextInt(seedRoute.size());
                }
            }
        }

        partial.removeEmptyRoutes();
        return new RemovalResult(partial, removed);
    }

    private List<Integer> collectCustomers(Solution solution) {
        List<Integer> customerIds = new ArrayList<>();
        for (Route route : solution.getRoutes()) {
            customerIds.addAll(route.getCustomerIdsView());
        }
        return customerIds;
    }

    private List<Route> collectNonEmptyRoutes(Solution solution) {
        List<Route> routes = new ArrayList<>();
        for (Route route : solution.getRoutes()) {
            if (!route.isEmpty()) {
                routes.add(route);
            }
        }
        return routes;
    }

    private void removeCustomer(Solution solution, int customerId) {
        for (Route route : solution.getRoutes()) {
            if (route.removeCustomer(customerId)) {
                return;
            }
        }
    }

    private boolean applyAdaptiveFacilityProcedure(Solution solution, List<Integer> removedCustomers) {
        boolean executed = false;
        double processProbability = Math.max(parameters.addProbabilityFloor, procedureScore.getWeight());
        if (random.nextDouble() <= processProbability) {
            addBikeOrHub(solution, removedCustomers);
            executed = true;
        }

        double shiftProbability = Math.max(parameters.shiftProbabilityFloor, procedureScore.getWeight());
        if (random.nextDouble() <= shiftProbability) {
            if (random.nextDouble() <= 0.5) {
                int gammaMax = (int) Math.ceil(Math.min(Math.max(5, 0.1 * instance.getCustomerCount()), 10));
                for (int g = 0; g < gammaMax; g++) {
                    shiftBike(solution, removedCustomers);
                }
            } else {
                shiftHub(solution, removedCustomers);
            }
            executed = true;
        }
        solution.removeEmptyRoutes();
        return executed;
    }

    private void addBikeOrHub(Solution solution, List<Integer> removedCustomers) {
        Set<Integer> openFacilities = solution.getOpenFacilityIds();
        Facility target = weightedFacilityChoice(openFacilities, removedCustomers, true);
        if (target == null) {
            return;
        }

        long existingRouteCount = solution.getRoutes().stream().filter(r -> r.getFacilityId() == target.getId()).count();
        if (target.getMaxVehicles() > 0 && existingRouteCount >= target.getMaxVehicles()) {
            return;
        }
        solution.addRoute(new Route(target.getId()));
    }

    private void shiftBike(Solution solution, List<Integer> removedCustomers) {
        List<Route> nonEmptyRoutes = collectNonEmptyRoutes(solution);
        if (nonEmptyRoutes.isEmpty()) {
            return;
        }
        Route selected = nonEmptyRoutes.get(random.nextInt(nonEmptyRoutes.size()));
        Set<Integer> candidates = new HashSet<>(solution.getOpenFacilityIds());
        for (Facility facility : instance.getFacilities()) {
            candidates.add(facility.getId());
        }
        candidates.remove(selected.getFacilityId());
        Facility newFacility = weightedFacilityChoice(candidates, removedCustomers, false);
        if (newFacility != null) {
            selected.setFacilityId(newFacility.getId());
        }
    }

    private void shiftHub(Solution solution, List<Integer> removedCustomers) {
        Set<Integer> openFacilities = solution.getOpenFacilityIds();
        if (openFacilities.size() <= 1) {
            return;
        }
        List<Integer> openList = new ArrayList<>(openFacilities);
        int fromFacilityId = openList.get(random.nextInt(openList.size()));

        Set<Integer> candidates = new HashSet<>();
        for (Facility facility : instance.getFacilities()) {
            if (facility.getId() != fromFacilityId) {
                candidates.add(facility.getId());
            }
        }
        Facility target = weightedFacilityChoice(candidates, removedCustomers, false);
        if (target == null) {
            return;
        }
        for (Route route : solution.getRoutes()) {
            if (route.getFacilityId() == fromFacilityId) {
                route.setFacilityId(target.getId());
            }
        }
    }

    private Facility weightedFacilityChoice(Set<Integer> candidateIds, List<Integer> removedCustomers, boolean allowClosedFacilities) {
        List<Facility> candidates = new ArrayList<>();
        for (int facilityId : candidateIds) {
            candidates.add(instance.getFacilityById(facilityId));
        }
        if (allowClosedFacilities) {
            for (Facility facility : instance.getFacilities()) {
                if (!candidateIds.contains(facility.getId())) {
                    candidates.add(facility);
                }
            }
        }
        if (candidates.isEmpty()) {
            return null;
        }

        Facility best = null;
        double bestScore = Double.POSITIVE_INFINITY;
        for (Facility facility : candidates) {
            double score = facility.getOpenCost();
            if (removedCustomers.isEmpty()) {
                for (Customer customer : instance.getCustomers()) {
                    score += distanceCalculator.distance(facility, customer) * customer.getDemand();
                }
            } else {
                for (int customerId : removedCustomers) {
                    Customer customer = instance.getCustomerById(customerId);
                    score += distanceCalculator.distance(facility, customer) * customer.getDemand();
                }
            }
            if (score < bestScore) {
                bestScore = score;
                best = facility;
            }
        }
        return best;
    }

    private void applyRepair(Solution solution, List<Integer> removedCustomers, RepairType repairType) {
        List<Integer> shuffled = new ArrayList<>(removedCustomers);
        Collections.shuffle(shuffled, random);
        for (int customerId : shuffled) {
            boolean useNoise = repairType == RepairType.GREEDY_INSERTION_WITH_NOISE;
            insertCustomerGreedy(solution, customerId, useNoise);
        }
        solution.removeEmptyRoutes();
    }

    private void insertCustomerGreedy(Solution solution, int customerId, boolean useNoise) {
        if (solution.getRoutes().isEmpty()) {
            Facility facility = chooseInitialFacility();
            Route route = new Route(facility.getId());
            route.addCustomer(customerId);
            solution.addRoute(route);
            return;
        }

        InsertionMove bestMove = null;
        for (int r = 0; r < solution.getRoutes().size(); r++) {
            Route route = solution.getRoutes().get(r);
            double customerDemand = instance.getCustomerById(customerId).getDemand();
            if (route.getDemand(instance) + customerDemand > instance.getVehicleCapacity() + 1e-9) {
                continue;
            }
            for (int pos = 0; pos <= route.size(); pos++) {
                double delta = evaluator.insertionDelta(route, customerId, pos);
                if (useNoise) {
                    delta *= parameters.noiseLower + random.nextDouble() * (parameters.noiseUpper - parameters.noiseLower);
                }
                if (bestMove == null || delta < bestMove.delta) {
                    bestMove = new InsertionMove(r, pos, delta);
                }
            }
        }

        if (bestMove == null) {
            Facility bestFacility = weightedFacilityChoice(solution.getOpenFacilityIds(), List.of(customerId), true);
            Route newRoute = new Route(bestFacility.getId());
            newRoute.addCustomer(customerId);
            solution.addRoute(newRoute);
        } else {
            solution.getRoutes().get(bestMove.routeIndex).addCustomer(bestMove.position, customerId);
        }
    }

    private void localSearch(Solution solution) {
        boolean improved = true;
        while (improved) {
            improved = false;
            for (Route route : solution.getRoutes()) {
                if (route.size() < 4) {
                    continue;
                }
                double bestRouteCost = evaluator.routeTravelCost(route);
                for (int i = 0; i < route.size() - 2; i++) {
                    for (int j = i + 1; j < route.size() - 1; j++) {
                        reverseSegment(route.getCustomerIds(), i, j);
                        double newCost = evaluator.routeTravelCost(route);
                        if (newCost + 1e-9 < bestRouteCost) {
                            bestRouteCost = newCost;
                            improved = true;
                        } else {
                            reverseSegment(route.getCustomerIds(), i, j);
                        }
                    }
                }
            }
        }
    }

    private void reverseSegment(List<Integer> list, int start, int end) {
        while (start < end) {
            Integer tmp = list.get(start);
            list.set(start, list.get(end));
            list.set(end, tmp);
            start++;
            end--;
        }
    }

    private boolean accept(double candidate, double current, double temperature) {
        if (candidate <= current) {
            return true;
        }
        double probability = Math.exp(-(candidate - current) / Math.max(temperature, 1e-9));
        return random.nextDouble() < probability;
    }

    private void updateOperatorWeights(DestroyType destroyType, RepairType repairType, double reward) {
        if (destroyType == DestroyType.RANDOM_REMOVAL) {
            randomRemovalScore.update(parameters.reactionFactor, reward);
        } else {
            sequenceRemovalScore.update(parameters.reactionFactor, reward);
        }

        if (repairType == RepairType.GREEDY_INSERTION) {
            greedyRepairScore.update(parameters.reactionFactor, reward);
        } else {
            noisyRepairScore.update(parameters.reactionFactor, reward);
        }
    }
}
