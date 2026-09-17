package tjTool.core;

import arc.scene.style.Drawable;

import static arc.Core.*;
import static mindustry.Vars.*;
import static tjTool.TheJourney.theJourney;

public final class TjVars {

    public static final int halfSize = tilesize / 2;

    public static boolean showConsPower = false;
    public static boolean showConsHeat = true;
    public static boolean showCraftTime = false;

    public static Drawable frame = atlas.drawable(theJourney.name + "-frame");

}
