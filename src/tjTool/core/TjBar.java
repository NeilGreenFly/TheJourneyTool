package tjTool.core;

import arc.Core;
import arc.func.Func;
import arc.graphics.Color;
import mindustry.gen.Building;
import mindustry.ui.Bar;

public class TjBar {

    public static Func<Building, Bar> makeHealthBalance() {
        return entity -> new Bar(
                () -> Core.bundle.get("stat.health", "rainbowHealth"),
                TjDraw::rainbow,
                entity::healthf
        ).blink(Color.white);
    }

}
