package test1.LRP_ALNS_Basline;

public class Facility extends Node {
    private final double openCost;
    private final double capacity;
    private final int maxVehicles;

    public Facility(int id, double x, double y, double openCost, double capacity, int maxVehicles) {
        super(id, x, y);
        this.openCost = openCost;
        this.capacity = capacity;
        this.maxVehicles = maxVehicles;
    }

    public double getOpenCost() {
        return openCost;
    }

    public double getCapacity() {
        return capacity;
    }

    public int getMaxVehicles() {
        return maxVehicles;
    }
}
