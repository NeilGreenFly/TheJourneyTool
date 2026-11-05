package tjTool.world.consumers;

import mindustry.gen.Building;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;

/**
 * 打算重写, 如果仅仅只是这样处理会有很多问题.<p>
 * 例如对于物品类可选消耗, 由于物品的消耗逻辑与流体不同, 它需要等待进度结束才会结算消耗,
 * 但是如果在进度结束前切换可选消耗, 在进入新一轮进度后再切换回来, 就可以以更低的成本使用可选消耗(甚至是无成本，如果可以的话).<p>
 * 很遗憾这是绝无可能被接受的, 而且为每一个可选消耗都添加独立的进度维护也是不太能被接受的, 尽管无可否认的是这的确有一定的可行性,
 * 但我们更愿意接受更好的方案.
 */
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
        return Math.max(super.efficiency(building) * (efficiency - 1) + 1, 1);
    }
}
