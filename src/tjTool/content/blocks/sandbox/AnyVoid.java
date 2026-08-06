package tjTool.content.blocks.sandbox;

import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.type.Liquid;
import mindustry.world.Block;
import mindustry.world.meta.BlockGroup;
import mindustry.world.modules.ItemModule;

public class AnyVoid extends SandboxBlock {
    public AnyVoid(String name) {
        super(name);
        acceptsItems = true;
        hasLiquids = true;
        liquidCapacity = 1000000f;
        group = BlockGroup.transportation;
    }

    @SuppressWarnings("unused")
    public class AnyVoidBuild extends SandboxBuild {

        @Override
        public Building create(Block block, Team team) {
            super.create(block, team);
            if (items == null) items = new ItemModule();
            return this;
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            return enabled;
        }

        @Override
        public void handleItem(Building source, Item item){
            items.handleFlow(item, 1);
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid) {
            return enabled;
        }

        @Override
        public void handleLiquid(Building source, Liquid liquid, float amount) {
            liquids.handleFlow(liquid, amount);
        }

        @Override
        public void placed() {
            super.placed();
            liquids.clear();
        }
    }
}
