package tjTool.world.blocks.anvil;

import arc.graphics.Color;
import mindustry.graphics.Layer;
import mindustry.world.draw.DrawDefault;
import mindustry.world.draw.DrawMulti;
import mindustry.world.draw.DrawRegion;
import tjTool.world.blocks.TjBlock;
import tjTool.world.draw.DrawBottom;
import tjTool.world.draw.DrawZ;

import static arc.math.geom.Geometry.d8edge;
import static mindustry.Vars.world;
import static tjTool.core.TjDraw.beacon;

public class Anvil extends TjBlock {
    protected static final int coreSize = 9;

    public Color color = Color.valueOf("#FFBFFF");

    public Anvil(String name) {
        super(name);
        size = coreSize;
        drawer = new DrawMulti(new DrawBottom(), new DrawZ(Layer.blockOver), new DrawRegion("-pillar"), new DrawDefault());
    }

    @SuppressWarnings("unused")
    public class AnvilBuild extends TjBuilding {
        @Override
        public void draw() {
            super.draw();
            beacon(x, y, 11 / 4f, color, 0.3f);
            beacon(x, y, 25 / 4f, color, 0.3f);
            beacon(x, y, 39 / 4f, color, 0.3f);
        }

        @Override
        public void onProximityUpdate() {
            for (var d : d8edge) if (world.build(tile.x + (size / 2 + 1) * d.x, tile.y + (size / 2 + 1) * d.y)
                    instanceof AnvilAmplifier.AnvilAmplifierBuild build && build.team == team)
                build.onProximityUpdate();
        }
    }
}
