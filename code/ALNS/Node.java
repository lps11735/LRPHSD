package test1.LRP_ALNS_Basline;

public abstract class Node {
    protected final int id;
    protected final double x;
    protected final double y;

    public Node(int id, double x, double y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public int getId() {
        return id;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
