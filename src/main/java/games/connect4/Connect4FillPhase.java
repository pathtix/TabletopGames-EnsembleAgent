package games.connect4;

public enum Connect4FillPhase {
    Opening(0.00, 0.25),
    Mid(0.25, 0.50),
    Late(0.50, 1.01);

    public final double low, high;
    Connect4FillPhase(double low, double high) {this.low = low; this.high = high; }

    public static Connect4FillPhase of(double fill) {
        for (Connect4FillPhase phase : values())
            if (fill >= phase.low && fill < phase.high) return phase;
        return Late;
    }
}
