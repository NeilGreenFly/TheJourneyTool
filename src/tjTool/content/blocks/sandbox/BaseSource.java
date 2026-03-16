package tjTool.content.blocks.sandbox;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.Mathf;
import arc.util.*;
import mindustry.Vars;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.type.*;
import mindustry.ui.Bar;
import mindustry.world.Block;
import mindustry.world.blocks.heat.HeatBlock;
import mindustry.world.draw.*;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.BuildVisibility;
import mindustry.world.meta.Env;
import tjTool.core.*;

import static mindustry.Vars.*;
import static mindustry.graphics.Layer.overlayUI;
import static mindustry.type.ItemStack.with;

public class BaseSource extends Block {
    public DrawBlock drawer = new DrawDefault();
    public float powerProduction = 1000000f / 60f;
    public float heatOutput = 1000f;
    public boolean drawProximity = false;

    public BaseSource(String name) {
        super(name);
        hasPower = true;
        outputsPower = true;
        consumesPower = false;
        conductivePower = true;
        rotateDraw = false;
        update = true;
        solid = true;
        canOverdrive = false;
        group = BlockGroup.transportation;
        schematicPriority = -9;
        configurable = false;
        saveConfig = false;
        clearOnDoubleTap = false;
        selectionRows = 5;
        selectionColumns = 6;
        noUpdateDisabled = true;
        envEnabled = Env.any;
        placeableLiquid = true;
        alwaysUnlocked = true;
    }

    public TextureRegion atlasFind(String suffix) {
        return Core.atlas.find(Vars.content.transformName(name + "-" + suffix));
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(TjStat.config, table -> {
            table.row();
            TjStat.acknowledgements(table, region);
        });
    }

    @Override
    public void setBars() {
        addBar("health", entity -> new Bar(
                () -> Core.bundle.get("stat.health", "stat.health"),
                TjDraw::rainbow,
                entity::healthf
        ).blink(Color.white));
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        if (valid && !rotate) {
            Draw.color(player.team().color);
            Draw.alpha(0.5f);
            Fill.square((x - 1) * tilesize, y * tilesize, 2 * size);
            Fill.square((x + 1) * tilesize, y * tilesize, 2 * size);
            Fill.square(x * tilesize, (y - 1) * tilesize, 2 * size);
            Fill.square(x * tilesize, (y + 1) * tilesize, 2 * size);
        }
    }

    public void requirements(Category cat) {
        requirements(cat, BuildVisibility.sandboxOnly, with());
    }

    @Override
    public void load() {
        super.load();
        drawer.load(this);
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) {
        drawer.drawPlan(this, plan, list);
    }

    @Override
    public TextureRegion[] icons() {
        return drawer.finalIcons(this);
    }

    public class BaseSourceBuild extends Building implements HeatBlock {
        private boolean selecting = false;
        private float selectingDrawRadius = 0f;

        public boolean checkBuild(Building other) {
            return other != null && other.interactable(team);
        }

        public void drawSelecting() {
            selectingDrawRadius = Mathf.lerpDelta(selectingDrawRadius, selecting ? 1f : -.1f, 0.1f);
            if (selectingDrawRadius > 0f) {
                Draw.z(overlayUI);
                TjDraw.lightPoly(x, y, 4, block.size * tilesize * selectingDrawRadius, team.color);
                selecting = false;
            }
        }

        @Override
        public void draw() {
            drawer.draw(this);
            drawSelecting();
        }

        @Override
        public void drawConfigure() {
            TjDraw.lightPoly(this, TjDraw.rainbow());
        }

        @Override
        public void drawLight() {
            super.drawLight();
            drawer.drawLight(this);
        }

        @Override
        public void drawSelect() {
            selecting = true;
            if (drawProximity && !rotate)
                for (var other: proximity)
                    if (checkBuild(other) && (other.block.hasItems || other.block.hasLiquids))
                        Drawf.selected(other.tile, team.color);
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            return false;
        }

        @Override
        public void handleItem(Building source, Item item) {}

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid) {
            return false;
        }

        @Override
        public void handleLiquid(Building source, Liquid liquid, float amount) {}

        @Override
        public float getPowerProduction() {
            return powerProduction;
        }

        @Override
        public float heat() {
            return heatOutput;
        }

        @Override
        public float heatFrac() {
            return 1f;
        }
    }
}
