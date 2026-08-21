package tjTool.world.consumers;

import mindustry.gen.Building;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;

@SuppressWarnings("unused")
public class OptionalConsumer extends BaseConsumer { // TODO 还没写完
    public float efficiency = 2;

    public OptionalConsumer(ItemStack[] items, LiquidStack[] liquids) {
        this.input = new MultiStack(items, liquids);
    }

    public static OptionalConsumer by(ItemStack[] items, LiquidStack[] liquids) {
        return new OptionalConsumer(items, liquids);
    }

    public OptionalConsumer efficiency(float efficiency) {
        this.efficiency = efficiency;
        return this;
    }

    @Override
    public float efficiency(Building building) {
        return super.efficiency(building) * efficiency;
    }
}
