package tjTool.widgets;

import arc.Core;
import arc.func.Func;
import arc.graphics.Color;
import arc.math.Mathf;
import mindustry.gen.Building;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import tjTool.world.graphics.TjDraw;

public final class TjBar {

    public static Func<Building, Bar> healthBar = build -> new Bar(
            () -> Core.bundle.get("stat.health", "rainbowHealth"),
            () -> TjDraw.rainbow,
            build::healthf).blink(Color.white);

    public static Func<Building, Bar> powerBar = build -> new Bar(
            () -> Core.bundle.get("bar.power"),
            () -> Pal.powerBar,
            () -> Mathf.zero(build.block.consPower.requestedPower(build)) && build.power.graph.getPowerProduced() + build.power.graph.getBatteryStored() > 0 ? 1 : build.power.status);

}
