package tjTool.world.blocks.sandbox;

import arc.Core;
import arc.graphics.g2d.*;
import arc.math.Mathf;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.graphics.*;
import mindustry.type.Item;
import mindustry.type.Liquid;
import mindustry.world.meta.Env;
import tjTool.core.*;
import tjTool.world.blocks.TjBlock;

import static mindustry.Vars.*;

public abstract class SandboxBlock extends TjBlock {
    public static Building input;

    public boolean drawProximity = false;

    public SandboxBlock(String name) {
        super(name);

        hasPower =
        outputsPower =
        consumesPower =
        conductivePower =
        hasItems =
        hasLiquids =
        unloadable = false;

        update = true;
        solid = true;

        configurable = false;
        saveConfig = false;
        clearOnDoubleTap = false;

        envEnabled = Env.any;
        schematicPriority = -9;
        canOverdrive = false;
        placeableLiquid = true;
        alwaysUnlocked = true;
    }

    public TextureRegion atlasFind(String suffix) {
        return Core.atlas.find(Vars.content.transformName(name + "-" + suffix));
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(TjStat.config, TjStat.acknowledgements(region));
    }

    @Override
    public void setBars() {
        addBar("health", TjBar.makeHealthBalance);
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        if (!rotate) TjDraw.drawPlace(this, x, y, valid);
    }

    @SuppressWarnings("unused")
    public abstract class SandboxBuild extends TjBuilding {
        protected float selectingDrawRadius = 0f;

        public boolean checkBuild(Building other) {
            return other != null && other.team == team;
        }

        public void drawSelecting() {
            if ((selectingDrawRadius = Mathf.lerpDelta(selectingDrawRadius, this == input ? 1f : -.1f, 0.1f)) > 0f) {
                Draw.z(Layer.overlayUI);
                TjDraw.lightPoly(x, y, 4, block.size * tilesize * selectingDrawRadius, team.color);
            }
        }

        @Override
        public void draw() {
            super.draw();
            drawSelecting();
        }

        @Override
        public void drawConfigure() {
            TjDraw.lightPoly(this, TjDraw.rainbow);
        }

        @Override
        public void drawSelect() {
            if (drawProximity && !rotate) proximity.each(
                    other -> checkBuild(other) && (other.block.hasItems || other.block.hasLiquids),
                    other -> Drawf.selected(other.tile, team.color));
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
    }
}
