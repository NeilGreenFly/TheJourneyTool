package tjTool.widgets;

import arc.scene.style.Drawable;

import static arc.Core.atlas;
import static tjTool.TheJourney.theJourney;

public class TjDrawable {

    public static Drawable frame;

    public static void load() {
        frame = atlas.drawable(theJourney.name + "-frame");
    }

}
