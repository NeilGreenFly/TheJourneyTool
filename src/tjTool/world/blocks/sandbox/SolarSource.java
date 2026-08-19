package tjTool.world.blocks.sandbox;

import arc.graphics.g2d.*;
import mindustry.gen.Building;
import mindustry.world.blocks.power.SolarGenerator;
import tjTool.core.*;
import tjTool.world.AutoTile;

import static arc.Core.atlas;
import static arc.math.geom.Geometry.d8;
import static mindustry.Vars.world;
import static mindustry.world.blocks.power.PowerNode.makeBatteryBalance;
import static mindustry.world.blocks.power.PowerNode.makePowerBalance;
import static tjTool.world.AutoTile.*;

public class SolarSource extends SolarGenerator {
    TextureRegion[] regions;

    public SolarSource(String name) {
        super(name);
        size = 1;
        health = 40;
        canOverdrive = false;
        placeableLiquid = true;
        alwaysUnlocked = true;
        allowRectanglePlacement = true;
        powerProduction = 1000000f / 60f;
    }

    @Override
    public void load() {
        super.load();
        regions = atlasRegions(atlas.find(name + "-atlas"), 32, 32, 0);
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(TjStat.config, TjStat.acknowledgements(region));
    }

    @Override
    public void setBars() {
        super.setBars();
        removeBar("power");
        addBar("health", TjBar.makeHealthBalance);
        addBar("power", makePowerBalance());
        addBar("batteries", makeBatteryBalance());
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        TjDraw.drawPlace(this, x, y, valid);
    }

    @SuppressWarnings("unused")
    public class SolarSourceBuild extends GeneratorBuild implements AutoTile {
        protected int index = 0;

        @Override
        public boolean checkBuild(Building other) {
            return other instanceof SolarSourceBuild && other.block == block && other.team == team;
        }

        @Override
        public void draw() {
            Draw.rect(regions[tileMap[index]], x, y);
        }

        @Override
        public void updateTile() {
            productionEfficiency = 1f;
        }

        @Override
        public void updateProximity() {
            super.updateProximity();
            proximityTileUpdate();
            for (var point : d8)
                if (world.build(tileX() + point.x, tileY() + point.y) instanceof SolarSourceBuild other && other.team == this.team)
                    other.proximityTileUpdate();
        }

        public void proximityTileUpdate() {
            index = getIndex8(this);
        }
    }
}
