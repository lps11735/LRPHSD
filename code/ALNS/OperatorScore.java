package test1.LRP_ALNS_Basline;

public class OperatorScore {
    private double weight;

    public OperatorScore(double initialWeight) {
        this.weight = initialWeight;
    }

    public double getWeight() {
        return weight;
    }

    public void update(double reactionFactor, double reward) {
        weight = reactionFactor * weight + (1.0 - reactionFactor) * reward;
    }
}
