package test1.LRP_ALNS_Basline;

public class ALNSParameters {
    public int randomSeed = 42;
    public int maxNoImprovement = 5000;
    public int maxIterations = 20000;
    public double reactionFactor = 0.85;
    public double initialOperatorWeight = 100.0;
    public double initialProcedureWeight = 0.5;
    public double localSearchThreshold = 0.07;
    public double temperature = 100.0;
    public double coolingRate = 0.95;
    public double rewardGlobalBest = 330.0;
    public double rewardCurrentBest = 130.0;
    public double rewardAcceptedWorse = 10.0;
    public double rewardRejected = 0.0;
    public double procedureRewardGlobalBest = 1.0;
    public double procedureRewardCurrentBest = 0.5;
    public double procedureRewardAcceptedWorse = 0.2;
    public double procedureRewardRejected = 0.0;
    public double noiseLower = 0.8;
    public double noiseUpper = 1.2;
    public double addProbabilityFloor = 0.05;
    public double shiftProbabilityFloor = 0.05;
}
