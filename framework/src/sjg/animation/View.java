package sjg.animation;

/**
 * View encapsulates a transformation between world and screen coordinates and vice versa.
 *
 * @author Christian Hvid
 */

public abstract interface View {
    public abstract int worldToRealX(double x);

    public abstract int worldToRealY(double y);

    public abstract double realToWorldX(int x);

    public abstract double realToWorldY(int y);
}
