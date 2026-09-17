package tjTool.world.blocks.liquid;

import arc.scene.ui.layout.Table;
import arc.util.Eachable;
import arc.util.Nullable;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.type.Liquid;
import mindustry.world.Block;
import mindustry.world.blocks.ItemSelection;
import mindustry.world.draw.DrawBlock;
import mindustry.world.draw.DrawLiquidTile;
import mindustry.world.draw.DrawMulti;
import mindustry.world.draw.DrawRegion;
import mindustry.world.meta.BlockGroup;
import tjTool.world.blocks.TjBlock;
import tjTool.world.draw.DrawBottom;

import static mindustry.Vars.content;
import static mindustry.world.blocks.liquid.LiquidBlock.drawTiledFrames;
import static tjTool.world.LazyGetter.*;

@SuppressWarnings("unused")
public class DirLiquidUnloader extends TjBlock {
    public static Liquid[] allLiquids;

    public DirLiquidUnloader(String name) {
        super(name);
        rotate = true;
        unloadable = false;
        hasLiquids = true;
        outputsLiquid = true;
        liquidCapacity = 0;
        update = true;
        configurable = true;
        saveConfig = true;
        clearOnDoubleTap = true;
        squareSprite = false;
        group = BlockGroup.liquids;
    }

    @Override
    protected DrawBlock defaultDrawer() {
        return new DrawMulti(new DrawBottom(), new DrawLiquidTile() {
            @Override
            public void drawPlan(Block block, BuildPlan plan, Eachable<BuildPlan> list) {
                if (plan.config instanceof Liquid liquid)
                    drawTiledFrames(size, plan.drawx(), plan.drawy(), 2.5f, liquid, 1);
            }

            @Override
            public void draw(Building build) {
                if (build instanceof DirLiquidUnloaderBuild unloader && unloader.sortLiquid != null)
                    drawTiledFrames(size, build.x, build.y, 2.5f, unloader.sortLiquid, 1);
            }
        }, new DrawRegion(), new DrawRegion("-top") {{ buildingRotate = true; }});
    }

    @Override
    protected void config() {
        config(Liquid.class, (DirLiquidUnloaderBuild build, Liquid v) -> build.sortLiquid = v);
        configClear((DirLiquidUnloaderBuild build) -> build.sortLiquid = null);
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
    public class DirLiquidUnloaderBuild extends TjBuilding {
        public @Nullable Liquid sortLiquid = null;
        public int currentId = 0;

        protected Liquid getNext(Building from) {
            if (sortLiquid != null || from == null) return sortLiquid;
            for (int i = 0; i < allLiquids.length; i += 1) {
                Liquid liquid = allLiquids[(currentId + i + 1) % allLiquids.length];
                var destination = from.getLiquidDestination(this, liquid);
                if (destination != from) return null;
                if (destination.block.hasLiquids && destination.liquids.get(liquid) > 0) return liquid;
            }
            return null;
        }

        @Override
        public void updateTile() {
            var back = back();
            var front = front();
            Liquid liquid = getNext(back);
            if (liquid == null || back == null || front == null) return;
            currentId = liquid.id;
            Building outBuilding = back.getLiquidDestination(this, liquid);
            Building inBuilding = front.getLiquidDestination(this, liquid);
            if (!(outBuilding == back && outBuilding.block.hasLiquids && outBuilding.liquids.get(liquid) > 0 &&
                    inBuilding != null && inBuilding.block.hasLiquids && inBuilding.acceptLiquid(this, liquid))) return;
            float amount = Math.min(outBuilding.liquids.get(liquid), inBuilding.block.liquidCapacity - inBuilding.liquids.get(liquid));
            outBuilding.liquids.remove(liquid, amount);
            inBuilding.handleLiquid(this, liquid, amount);
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
