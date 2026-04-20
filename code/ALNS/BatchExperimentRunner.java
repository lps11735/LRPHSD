package test1.LRP_ALNS_Basline;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BatchExperimentRunner {

    private static final int RUNS_PER_INSTANCE = 5;

    public static void main(String[] args) {
        try {
            // Modify these paths before running
            String rootFolder = "/Users/lps/IdeaProjects/Project1/src/test1/LRP_ALNS_Basline/LRPInstances";
            String summaryCsvPath = "/Users/lps/IdeaProjects/Project1/summary.csv";
            String detailsCsvPath = "/Users/lps/IdeaProjects/Project1/details.csv";

            runBatch(rootFolder, summaryCsvPath, detailsCsvPath);

        } catch (Exception e) {
            System.err.println("Batch experiment terminated with an error.");
            e.printStackTrace();
        }
    }

    public static void runBatch(String rootFolder, String summaryCsvPath, String detailsCsvPath) throws Exception {
        List<File> instanceFiles = collectInstanceFiles(new File(rootFolder));

        if (instanceFiles.isEmpty()) {
            throw new IllegalArgumentException("No instance files found under: " + rootFolder);
        }

        instanceFiles.sort(Comparator.comparing(File::getAbsolutePath));

        try (BufferedWriter summaryWriter = new BufferedWriter(new FileWriter(summaryCsvPath));
             BufferedWriter detailWriter = new BufferedWriter(new FileWriter(detailsCsvPath))) {

            // Write headers
            summaryWriter.write(
                    "instance_type,instance_name,instance_path,runs," +
                            "best_objective,best_penalized_objective,best_feasible," +
                            "average_objective,average_penalized_objective,average_runtime_sec,average_feasible_rate"
            );
            summaryWriter.newLine();

            detailWriter.write(
                    "instance_type,instance_name,instance_path,run_id,seed," +
                            "objective,penalized_objective,feasible,runtime_sec"
            );
            detailWriter.newLine();

            for (File instanceFile : instanceFiles) {
                String instanceName = instanceFile.getName();
                String instanceType = inferInstanceType(instanceFile);
                String instancePath = instanceFile.getAbsolutePath();

                System.out.println("========================================");
                System.out.println("Processing instance: " + instanceName);
                System.out.println("Type: " + instanceType);
                System.out.println("Path: " + instancePath);

                LRPInstance instance = InstanceLoader.load(instanceType, instancePath);

                double bestObjective = Double.POSITIVE_INFINITY;
                double bestPenalizedObjective = Double.POSITIVE_INFINITY;
                double sumObjective = 0.0;
                double sumPenalizedObjective = 0.0;
                double sumRuntimeSec = 0.0;
                int feasibleCount = 0;
                boolean bestFeasible = false;

                for (int run = 1; run <= RUNS_PER_INSTANCE; run++) {
                    ALNSParameters params = new ALNSParameters();
                    params.randomSeed = (int) ((System.nanoTime() + run * 10007L) & 0x7fffffff);

                    long startTime = System.nanoTime();
                    ALNSSolver solver = new ALNSSolver(instance, params);
                    Solution solution = solver.solve();
                    long endTime = System.nanoTime();

                    SolutionEvaluator evaluator = new SolutionEvaluator(instance);
                    evaluator.evaluate(solution);

                    double objective = solution.getObjectiveValue();
                    double penalizedObjective = solution.getPenalizedObjectiveValue();
                    boolean feasible = evaluator.isFeasible(solution);
                    double runtimeSec = (endTime - startTime) / 1_000_000_000.0;

                    if (objective < bestObjective) {
                        bestObjective = objective;
                    }
                    if (penalizedObjective < bestPenalizedObjective) {
                        bestPenalizedObjective = penalizedObjective;
                        bestFeasible = feasible;
                    }

                    sumObjective += objective;
                    sumPenalizedObjective += penalizedObjective;
                    sumRuntimeSec += runtimeSec;
                    if (feasible) {
                        feasibleCount++;
                    }

                    System.out.printf(
                            "Run %d | objective = %.3f | penalized = %.3f | feasible = %s | time = %.6f s | seed = %d%n",
                            run, objective, penalizedObjective, feasible, runtimeSec, params.randomSeed
                    );

                    writeCsvRow(detailWriter,
                            instanceType,
                            instanceName,
                            instancePath,
                            String.valueOf(run),
                            String.valueOf(params.randomSeed),
                            formatDouble(objective),
                            formatDouble(penalizedObjective),
                            String.valueOf(feasible),
                            formatDouble(runtimeSec)
                    );
                }

                double avgObjective = sumObjective / RUNS_PER_INSTANCE;
                double avgPenalizedObjective = sumPenalizedObjective / RUNS_PER_INSTANCE;
                double avgRuntimeSec = sumRuntimeSec / RUNS_PER_INSTANCE;
                double avgFeasibleRate = (double) feasibleCount / RUNS_PER_INSTANCE;

                System.out.printf(
                        "Summary | best obj = %.3f | best penalized = %.3f | best feasible = %s | avg obj = %.3f | avg penalized = %.3f | avg time = %.6f s | feasible rate = %.2f%n",
                        bestObjective, bestPenalizedObjective, bestFeasible, avgObjective, avgPenalizedObjective, avgRuntimeSec, avgFeasibleRate
                );

                writeCsvRow(summaryWriter,
                        instanceType,
                        instanceName,
                        instancePath,
                        String.valueOf(RUNS_PER_INSTANCE),
                        formatDouble(bestObjective),
                        formatDouble(bestPenalizedObjective),
                        String.valueOf(bestFeasible),
                        formatDouble(avgObjective),
                        formatDouble(avgPenalizedObjective),
                        formatDouble(avgRuntimeSec),
                        formatDouble(avgFeasibleRate)
                );
            }
        }

        System.out.println("========================================");
        System.out.println("Batch experiment finished.");
        System.out.println("Summary CSV saved to: " + summaryCsvPath);
        System.out.println("Details CSV saved to: " + detailsCsvPath);
    }

    private static List<File> collectInstanceFiles(File root) throws IOException {
        List<File> result = new ArrayList<>();

        Files.walk(root.toPath())
                .filter(Files::isRegularFile)
                .map(java.nio.file.Path::toFile)
                .forEach(file -> {
                    String fileName = file.getName().toLowerCase();
                    String path = file.getAbsolutePath().toLowerCase();

                    // Skip format descriptions and hidden files
                    if (fileName.startsWith("format")) {
                        return;
                    }
                    if (fileName.startsWith(".")) {
                        return;
                    }

                    // Keep all regular instance files inside the five instance folders
                    if (path.contains("instances-f1")
                            || path.contains("instances-f2")
                            || path.contains("instances-f3")
                            || path.contains("instances-f4")
                            || path.contains("instances-f5")) {
                        result.add(file);
                    }
                });

        return result;
    }

    private static String inferInstanceType(File instanceFile) {
        String path = instanceFile.getAbsolutePath().toUpperCase();

        if (path.contains("INSTANCES-F1")) {
            return "F1";
        }
        if (path.contains("INSTANCES-F2")) {
            return "F2";
        }
        if (path.contains("INSTANCES-F3")) {
            return "F3";
        }
        if (path.contains("INSTANCES-F4")) {
            return "F4";
        }
        if (path.contains("INSTANCES-F5")) {
            return "F5";
        }

        throw new IllegalArgumentException("Cannot infer instance type from path: " + instanceFile.getAbsolutePath());
    }

    private static void writeCsvRow(BufferedWriter writer, String... values) throws IOException {
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                writer.write(",");
            }
            writer.write(escapeCsv(values[i]));
        }
        writer.newLine();
    }

    private static String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        boolean needsQuotes = value.contains(",") || value.contains("\"") || value.contains("\n");
        if (needsQuotes) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }
        return value;
    }

    private static String formatDouble(double value) {
        return String.format("%.6f", value);
    }
}