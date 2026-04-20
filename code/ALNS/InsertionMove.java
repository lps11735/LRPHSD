package test1.LRP_ALNS_Basline;

public class InsertionMove {
    public final int routeIndex;
    public final int position;
    public final double delta;

    public InsertionMove(int routeIndex, int position, double delta) {
        this.routeIndex = routeIndex;
        this.position = position;
        this.delta = delta;
    }
}
