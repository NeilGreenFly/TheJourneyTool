package tjTool.world.blocks.anvil;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.scene.ui.ImageButton;
import arc.scene.ui.layout.Table;
import arc.util.Nullable;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.Planet;
import mindustry.ui.Bar;
import mindustry.ui.Styles;
import mindustry.world.draw.DrawBlock;
import mindustry.world.draw.DrawDefault;
import mindustry.world.draw.DrawMulti;
import mindustry.world.draw.DrawRegion;
import tjTool.world.blocks.TjBlock;
import tjTool.world.draw.DrawBottom;
import tjTool.world.draw.DrawZ;
import tjTool.world.graphics.AnvilRingRender;

import static arc.math.geom.Geometry.d8edge;
import static mindustry.Vars.*;
import static tjTool.core.TjDraw.beacon;
import static tjTool.core.TjTable.uiSize;
import static tjTool.world.LazyGetter.w;
import static tjTool.world.graphics.AnvilRingRender.z;

// 困困...睡觉觉喵...
public class Anvil extends TjBlock {
    protected static final int coreSize = 9;

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
    protected void config() {
        config(Planet.class, (AnvilBuild build, Planet v) -> build.planet = v);
        configClear((AnvilBuild build) -> build.planet = null);
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
        public @Nullable Planet planet;
        public float delta = 0;
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
            beacon(1.2f);
            beacon(x, y, 11 / 4f, color, 0.3f);
            beacon(x, y, 25 / 4f, color, 0.3f);
            beacon(x, y, 39 / 4f, color, 0.3f);
            if (delta > 0) Draw.draw(Layer.flyingUnit - 2, () -> AnvilRingRender.render(x, y, planet, delta));
        }

        @Override
        public void onProximityUpdate() {
            for (var d : d8edge) if (world.build(tile.x + (size / 2 + 1) * d.x, tile.y + (size / 2 + 1) * d.y)
                    instanceof AnvilAmplifier.AnvilAmplifierBuild build && build.team == team)
                build.onProximityUpdate();
        }

        @Override
        public void updateTile() {
            delta = Mathf.lerp(delta, Mathf.num(planet != null), 0.1f);
        }

        @Override
        public void buildConfiguration(Table table) {
            table.background(Tex.paneLeft).table(t -> {
                for (var v : content.planets()) {
                    var button = new ImageButton(Icon.icons.get(v.icon, Icon.commandRally), Styles.clearNoneTogglei);
                    button.getStyle().imageUpColor = v.iconColor;
                    button.update(() -> button.setChecked(v == planet));
                    button.changed(() -> {
                        configure(button.isChecked() ? v : null);
                        deselect();
                    });
                    t.add(button).size(uiSize).color(v.iconColor).tooltip(v.localizedName);
                }
            }).row();
            table.slider(0, 10, 0.01f, z, v -> z = v).growX().padTop(10).row();
        }

        @Override
        public boolean onConfigureBuildTapped(Building other) {
            if (this == other) deselect();
            return this != other;
        }

        @Override
        public void drawConfigure() {
            Draw.color(Pal.accent);
            Draw.rect(drawConfigure, x, y);
            Draw.reset();
        }

        @Override
        public Object config() {
            return planet;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.s(w(planet));
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            var v = read.s();
            if (-1 < v && v < content.planets().size) planet = content.planets().get(v);
        }
    }
}
