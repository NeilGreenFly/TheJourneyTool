package tjTool.world.blocks.sandbox;

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

    @Override
    public boolean canReplace(Block other) {
        return other.alwaysReplace || !other.privileged && other.replaceable && (other != this || (rotate && quickRotate)) &&
                ((group != BlockGroup.none && (other.group == BlockGroup.transportation || other.group == BlockGroup.liquids)) || other == this) &&
                (size == other.size || (size >= other.size && ((subclass != null && subclass == other.subclass) || group.anyReplace)));
    }

    public class BuildType extends SandboxBlock.BuildType {

        @Override
        public Building create(Block block, Team team) {
            super.create(block, team);
            if (items == null) items = new ItemModule();
            return this;
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            return enabled;
        }

        @Override
        public void handleItem(Building source, Item item) {
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
