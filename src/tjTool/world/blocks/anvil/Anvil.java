package tjTool.world.blocks.anvil;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.scene.ui.layout.Table;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.world.draw.DrawBlock;
import mindustry.world.draw.DrawDefault;
import mindustry.world.draw.DrawMulti;
import mindustry.world.draw.DrawRegion;
import tjTool.world.blocks.TjBlock;
import tjTool.world.draw.DrawBottom;
import tjTool.world.draw.DrawZ;

import static arc.math.geom.Geometry.d8edge;
import static mindustry.Vars.world;
import static tjTool.core.TjDraw.beacon;
import static tjTool.core.TjVars.frame;

// 困困...睡觉觉喵...
public class Anvil extends TjBlock {
    protected static final int coreSize = 9;

    public TextureRegion drawConfigure;

    public Color color = Color.valueOf("#FFBFFF");
    public float energyCapacity = 100;

    public Anvil(String name) {
        super(name);
        size = coreSize;
        configurable = true;
    }

    @Override
    protected DrawBlock defaultDrawer() {
        return new DrawMulti(new DrawBottom(), new DrawZ(Layer.blockOver), new DrawRegion("-pillar"), new DrawDefault());
    }

    @Override
    public void load() {
        super.load();
        drawConfigure = Core.atlas.find(name + "-dc");
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("E", (AnvilBuild build) -> new Bar(
                () -> "EE",
                () -> color,
                () -> build.energy / energyCapacity
        ));
    }

    @SuppressWarnings("unused")
    public class AnvilBuild extends TjBuilding {
        public float energy = 0;

        public float acceptEnergy() {
            return energyCapacity - energy;
        }

        public void handleEnergy(float amount) {
            energy += amount;
        }

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

        @Override
        public void buildConfiguration(Table table) {
            table.background(frame).table(t -> {
                t.add("Stay tuned").row();
                t.add("敬请期待").row();
            }).pad(50);
        }

        @Override
        public void drawConfigure() {
            Draw.color(Pal.accent);
            Draw.rect(drawConfigure, x, y);
            Draw.reset();
        }
    }
}
