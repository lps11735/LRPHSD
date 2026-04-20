package test1.LRP_ALNS_Basline;

public class DistanceCalculator {
    private final LRPInstance instance;

    public DistanceCalculator(LRPInstance instance) {
        this.instance = instance;
    }

    public double distance(Node a, Node b) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        double base = Math.sqrt(dx * dx + dy * dy);

        return switch (instance.getDistanceMode()) {
            case EUCLIDEAN -> base;
            case CEIL_EUCLIDEAN -> Math.ceil(base);
            case ROUND_EUCLIDEAN -> Math.rint(base);
            case TRUNCATED_EUCLIDEAN_X100 -> Math.ceil(base * 100.0);
            //case TRUNCATED_EUCLIDEAN_X100 -> Math.floor(base) * 100.0;
        };
    }
}