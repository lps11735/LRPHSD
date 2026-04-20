package test1.LRP_ALNS_Basline;

public class Customer extends Node {
    private final double demand;

    public Customer(int id, double x, double y, double demand) {
        super(id, x, y);
        this.demand = demand;
    }

    public double getDemand() {
        return demand;
    }
}
