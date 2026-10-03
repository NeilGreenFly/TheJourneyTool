package tjTool.world.blocks.liquid;

import mindustry.gen.Building;
import mindustry.type.Liquid;
import mindustry.world.Tile;
import mindustry.world.blocks.liquid.Conduit;
import mindustry.world.draw.*;
import mindustry.world.meta.BlockGroup;
import tjTool.world.blocks.TjBlock;
import tjTool.world.draw.DrawBottom;

import static mindustry.Vars.world;

public class DirLiquidRouter extends TjBlock {
    public DirLiquidRouter(String name) {
        super(name);
        rotate = true;
        unloadable = false;
        hasLiquids = true;
        outputsLiquid = true;
        liquidCapacity = 150f;
        update = true;
        squareSprite = false;
        underBullets = true;
        solid = false;
        group = BlockGroup.liquids;
    }

    @Override
    protected DrawBlock defaultDrawer() {
        return new DrawMulti(new DrawBottom(), new DrawLiquidTile() {{ padding = 1.75f; }}, new DrawRegion(), new DrawRegion("-top") {{ buildingRotate = true; }});
    }

    @Override
    public boolean rotatedOutput(int fromX, int fromY, Tile destination) {
        return destination.build instanceof Conduit.ConduitBuild && world.build(fromX, fromY) instanceof DirLiquidRouterBuild self && destination.relativeTo(fromX, fromY) == self.rotation;
    }

    @SuppressWarnings("unused")
    public class DirLiquidRouterBuild extends TjBuilding {
        @Override
        public boolean acceptLiquid(Building source, Liquid liquid) {
            return source.relativeTo(this) == rotation && (liquids.current() == liquid || liquids.currentAmount() < 0.2f);
        }

        @Override
        public void updateTile() {
            for (int i = -1; i < 2; i += 1) dumpLiquid(liquids.current(), 2, i);
        }
    }
}
