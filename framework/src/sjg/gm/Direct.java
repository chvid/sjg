package sjg.gm;

import sjg.GraphicsModel;
import sjg.SJGame;

import java.awt.*;

/**
 * A {@link GraphicsModel graphics model} that provides direct unbuffered access to the game window.
 *
 * @author Christian Hvid
 */

public class Direct extends GraphicsModel {
    public Direct(SJGame sjg) {
        super(sjg);
    }

    protected void startDraw(Graphics g) {
        setFront(g);
    }
}
