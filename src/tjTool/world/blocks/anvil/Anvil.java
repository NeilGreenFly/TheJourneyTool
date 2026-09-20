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
import tjTool.core.TjCube;
import tjTool.world.blocks.TjBlock;
import tjTool.world.draw.DrawBottom;
import tjTool.world.draw.DrawZ;

import static arc.math.geom.Geometry.d8edge;
import static mindustry.Vars.world;
import static tjTool.content.TjEvents.update;
import static tjTool.core.TjCube.*;
import static tjTool.core.TjDraw.beacon;
import static tjTool.core.TjFunc.forRange;
import static tjTool.core.TjVars.frame;

// 困困...睡觉觉喵...
public class Anvil extends TjBlock {
    protected static final int coreSize = 9;
    protected static final TjCube cube = new TjCube().setScale(10);
    protected static final TjCube circle = new TjCube().setScale(10);
    protected static final TjCube circleLarge = new TjCube().setScale(10);

    {
        cube.rotationSpeed.set(1f, 0.5f, 0);
        forRange(2, x -> forRange(2, y -> forRange(2, z -> cube.add(x * 2 - 1, y * 2 - 1, z * 2 - 1))));
        cube.areas.addAll(cubeArea);
        update(cube::update);
        // cube.edges.addAll(new int[]{0, 4}, new int[]{1, 5}, new int[]{2, 6}, new int[]{3, 7}, new int[]{0, 2}, new int[]{1, 3}, new int[]{4, 6}, new int[]{5, 7}, new int[]{0, 1}, new int[]{2, 3}, new int[]{4, 5}, new int[]{6, 7});
        circle.rotationSpeed.set(0.2f, 0.2f, 0);
        forRange(2, x -> forRange(2, y -> forRange(2, z -> circle.add(x * 2 - 1, y * 2 - 1, (z * 2 - 1) * 0.1f))));
        circle.areas.addAll(cubeAreaWithoutZ);
        update(circle::update);
        circleLarge.rotationSpeed.set(0.4f, 0, 0);
        forRange(2, x -> forRange(2, y -> forRange(2, z -> circleLarge.add(x * 2 - 1, y * 2 - 1, (z * 2 - 1) * 0.1f))));
        circleLarge.areas.addAll(cubeAreaWithoutZ);
        update(() -> {
            circleLarge.rotation.y = circle.rotation.y;
            circleLarge.update();
        });
    }

    public TextureRegion drawConfigure;

    public Color color = Color.valueOf("#FFBFFF");
    public Color outline = Color.valueOf("#FE00FF");
    public Color in = Color.valueOf("#6A00A2");
    public float energyCapacity = 100;

    public Anvil(String name) {
        super(name);
        size = coreSize;
        configurable = true;
        clearOnDoubleTap = true;
        update = true;
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
            Draw.z(Layer.flyingUnit);
            TjCube.at(this);
            circleLarge.config(6, 1, false).fill(team.color);
            circleLarge.config(5.8f, 0.9f, false).fill(team.color);
            circle.config(4, 1, false).fill(team.color);
            circle.config(3.8f, 0.9f, false).fill(team.color);
            cube.config(2, 0.75f, false).fill(outline);
            cube.config(1.5f, 0.75f, true).fill(in);
            circle.config(3.8f, 0.9f, true).fill(team.color);
            circle.config(4, 1, true).fill(team.color);
            circleLarge.config(5.8f, 0.9f, true).fill(team.color);
            circleLarge.config(6, 1, true).fill(team.color);
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
