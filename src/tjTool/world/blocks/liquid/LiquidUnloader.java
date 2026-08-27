package tjTool.world.blocks.liquid;

import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Eachable;
import arc.util.Nullable;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.type.Liquid;
import mindustry.world.Block;
import mindustry.world.blocks.ItemSelection;
import mindustry.world.draw.DrawLiquidTile;
import mindustry.world.draw.DrawMulti;
import mindustry.world.draw.DrawRegion;
import mindustry.world.meta.BlockGroup;
import tjTool.world.draw.DrawBottom;
import tjTool.world.blocks.TjBlock;

import static mindustry.Vars.content;
import static mindustry.world.blocks.liquid.LiquidBlock.*;

public class LiquidUnloader extends TjBlock {
    public static Liquid[] allLiquids;

    public LiquidUnloader(String name) {
        super(name);
        solid = true;
        unloadable = false;
        hasLiquids = true;
        outputsLiquid = true;
        liquidCapacity = 0;
        update = true;
        noUpdateDisabled = true;
        configurable = true;
        saveConfig = true;
        clearOnDoubleTap = true;
        squareSprite = false;
        group = BlockGroup.liquids;
        drawer = new DrawMulti(new DrawBottom(), new DrawLiquidTile() {
            @Override
            public void drawPlan(Block block, BuildPlan plan, Eachable<BuildPlan> list) {
                if (plan.config instanceof Liquid liquid)
                    drawTiledFrames(size, plan.drawx(), plan.drawy(), 2.5f, liquid, 1);
            }

            @Override
            public void draw(Building build) {
                if (build instanceof LiquidUnloaderBuild unloader && unloader.sortLiquid != null)
                    drawTiledFrames(size, build.x, build.y, 2.5f, unloader.sortLiquid, 1);
            }
        }, new DrawRegion());
    }

    @Override
    protected void config() {
        config(Liquid.class, (LiquidUnloaderBuild build, Liquid v) -> build.sortLiquid = v);
        configClear((LiquidUnloaderBuild build) -> build.sortLiquid = null);
    }

    @Override
    public void init() {
        super.init();
        allLiquids = content.liquids().toArray(Liquid.class);
    }

    @Override
    public void setBars() {
        super.setBars();
        removeBar("liquid");
    }

    @SuppressWarnings("unused")
    public class LiquidUnloaderBuild extends TjBuilding {
        protected static final Seq<Building> proximityBuilding = new Seq<>();
        protected static final Seq<Building> outBuilding = new Seq<>();
        protected static final Seq<Building> inBuilding = new Seq<>();

        public @Nullable Liquid sortLiquid = null;
        public int currentId = 0;

        protected Liquid getNext() {
            if (sortLiquid != null) return sortLiquid;
            for (int i = 0; i < allLiquids.length; i += 1) {
                Liquid liquid = allLiquids[(currentId + i + 1) % allLiquids.length];
                int in = 0;
                int out = 0;
                for (var building : proximity) {
                    var destination = building.getLiquidDestination(this, liquid);
                    if (!destination.block.hasLiquids) continue;
                    if (destination.acceptLiquid(this, liquid)) in += 1;
                    if (destination.liquids.get(liquid) > 0) out += 1;
                }
                if (in > 0 && out > 0) return liquid;
            }
            return null;
        }

        @Override
        public void updateTile() {
            Liquid liquid = getNext();
            if (liquid == null) return;
            currentId = liquid.id;
            float totalCapacity = 0;
            float totalLiquid = 0;
            float outLiquid = 0;
            float inLiquid = 0;
            proximityBuilding.clear();
            outBuilding.clear();
            inBuilding.clear();
            for (var building : proximity) {
                Building destination = building.getLiquidDestination(this, liquid);
                if (!destination.block.hasLiquids) continue;
                if (!destination.acceptLiquid(this, liquid)) {
                    float get = destination.liquids.get(liquid);
                    if (get == 0) continue;
                    outLiquid += get;
                    outBuilding.add(destination);
                } else if (destination == building) {
                    totalCapacity += destination.block.liquidCapacity;
                    totalLiquid += destination.liquids.get(liquid);
                    proximityBuilding.add(destination);
                } else {
                    inLiquid += destination.block.liquidCapacity - destination.liquids.get(liquid);
                    inBuilding.add(destination);
                }
            }
            totalLiquid += outLiquid;
            float inPer = inLiquid == 0 ? 0 : Mathf.clamp(totalLiquid / inLiquid);
            totalLiquid -= inLiquid * inPer;
            float per = totalCapacity == 0 ? 0 : Mathf.clamp(totalLiquid / totalCapacity);
            float outPer = outLiquid == 0 ? 0 : 1 - Mathf.maxZero(totalLiquid - totalCapacity) / outLiquid;
            for (var b : inBuilding) b.handleLiquid(this, liquid, (b.block.liquidCapacity - b.liquids.get(liquid)) * inPer);
            for (var b : outBuilding) b.liquids.remove(liquid, b.liquids.get(liquid) * outPer);
            for (var b : proximityBuilding) {
                var to = b.block.liquidCapacity * per;
                b.handleLiquid(this, liquid, to - b.liquids.get(liquid));
            }
        }

        @Override
        public void buildConfiguration(Table table) {
            ItemSelection.buildTable(block, table, content.liquids(), () -> sortLiquid, this::configure, selectionRows, selectionColumns);
        }

        @Override
        public Object config() {
            return sortLiquid;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.s(w(sortLiquid));
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            sortLiquid = content.liquid(read.s());
        }
    }
}
