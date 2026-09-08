package tjTool.world.blocks.liquid;

import arc.scene.ui.layout.Table;
import arc.util.Nullable;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.gen.Building;
import mindustry.type.Liquid;
import mindustry.world.blocks.ItemSelection;
import mindustry.world.meta.BlockGroup;
import tjTool.world.blocks.TjBlock;

import static mindustry.Vars.content;

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
        // drawer =
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

        protected Liquid getNext() {
            if (sortLiquid != null) return sortLiquid;
            for (int i = 0; i < allLiquids.length; i += 1) {
                Liquid liquid = allLiquids[(currentId + i + 1) % allLiquids.length];
                var destination = back().getLiquidDestination(this, liquid);
                if (destination != null && destination.block.hasLiquids && destination.liquids.get(liquid) > 0) return liquid;
            }
            return null;
        }

        @Override
        public void updateTile() {
            Liquid liquid = getNext();
            if (liquid == null) return;
            currentId = liquid.id;
            Building outBuilding = back().getLiquidDestination(this, liquid);
            Building inBuilding = front().getLiquidDestination(this, liquid);
            if (!(outBuilding != null && outBuilding.block.hasLiquids && outBuilding.liquids.get(liquid) > 0 && inBuilding != null && inBuilding.block.hasLiquids && inBuilding.acceptLiquid(this, liquid))) return;
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
