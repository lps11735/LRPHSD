package test1.LRP_ALNS_Basline;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        try {
            //choose instance class
            String instanceType = "F5";

            String instancePath;
            if (args != null && args.length > 0) {
                instancePath = args[0];
            } else {
                instancePath = "PATH";
                // Example:
                // instancePath = "/Users/lps/IdeaProjects/Project1/data/r30x5a-1.txt";
            }

            LRPInstance instance = InstanceLoader.load(instanceType, instancePath);

            System.out.println("Instance loaded successfully.");
            System.out.println("Customers: " + instance.getCustomerCount());
            System.out.println("Facilities: " + instance.getFacilityCount());
            System.out.println("Vehicle capacity: " + instance.getVehicleCapacity());
            System.out.println("Vehicle fixed cost: " + instance.getVehicleFixedCost());
            System.out.println("Distance cost factor: " + instance.getDistanceCostFactor());
            System.out.println("Distance mode: " + instance.getDistanceMode());
            System.out.println("====================================");

            int runs = 5;
            List<Double> objectiveValues = new ArrayList<>();
            List<Double> runtimesSeconds = new ArrayList<>();

            double bestObjective = Double.POSITIVE_INFINITY;
            Solution bestSolutionOverall = null;
            int bestRunIndex = -1;
            int bestSeed = -1;

            for (int run = 1; run <= runs; run++) {
                ALNSParameters params = new ALNSParameters();

                // Use different seed for each run
                int seed = (int) ((System.nanoTime() + run * 10007L) & 0x7fffffff);
                params.randomSeed = seed;

                System.out.println("Run " + run + " started. Seed = " + seed);

                ALNSSolver solver = new ALNSSolver(instance, params);

                long startTime = System.nanoTime();
                Solution solution = solver.solve();
                long endTime = System.nanoTime();

                SolutionEvaluator evaluator = new SolutionEvaluator(instance);
                evaluator.evaluate(solution);

                boolean feasible = evaluator.isFeasible(solution);

                System.out.printf(
                        "objective = %.3f | penalized = %.3f | feasible = %s%n",
                        solution.getObjectiveValue(),
                        solution.getPenalizedObjectiveValue(),
                        feasible
                );
                evaluator.evaluate(solution);

                //double objective = solution.getObjectiveValue();
                double objective = solution.getPenalizedObjectiveValue();
                double runtimeSeconds = (endTime - startTime) / 1_000_000_000.0;

                objectiveValues.add(objective);
                runtimesSeconds.add(runtimeSeconds);

                System.out.printf("Run %d finished | objective = %.3f | time = %.6f s%n",
                        run, objective, runtimeSeconds);

                if (objective < bestObjective) {
                    bestObjective = objective;
                    bestSolutionOverall = new Solution(solution);
                    bestRunIndex = run;
                    bestSeed = seed;
                }

                System.out.println("------------------------------------");
            }

            double avgObjective = average(objectiveValues);
            double avgRuntime = average(runtimesSeconds);

            System.out.println("============== Summary ==============");
            System.out.printf("Best objective: %.3f%n", bestObjective);
            System.out.printf("Average objective: %.3f%n", avgObjective);
            System.out.printf("Average runtime: %.6f s%n", avgRuntime);
            System.out.printf("Best run: %d%n", bestRunIndex);
            System.out.printf("Best seed: %d%n", bestSeed);
            System.out.println("=====================================");

            if (bestSolutionOverall != null) {
                SolutionEvaluator evaluator = new SolutionEvaluator(instance);
                evaluator.evaluate(bestSolutionOverall);
                SolutionPrinter.print(instance, bestSolutionOverall);
            }

        } catch (Exception e) {
            System.err.println("Program terminated with an error.");
            e.printStackTrace();
        }
    }

    private static double average(List<Double> values) {
        if (values == null || values.isEmpty()) {
            return 0.0;
        }
        double sum = 0.0;
        for (double v : values) {
            sum += v;
        }
        return sum / values.size();
    }
}