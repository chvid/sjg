package sjg.animation;

/**
 * Maps world coordinates directly to screen coordinates. Singleton.
 * <p>
 * Default view.
 *
 * @author Christian Hvid
 */

public class NullView implements View {
    private static NullView instance;

    public static NullView getInstance() {
        if (instance == null)
            instance = new NullView();

        return instance;
    }

    private NullView() {
    }

    public int worldToRealX(double x) {
        return (int) x;
    }

    public int worldToRealY(double y) {
        return (int) y;
    }

    public double realToWorldX(int x) {
        return x;
    }

    public double realToWorldY(int y) {
        return y;
    }
}
