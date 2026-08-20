package tjTool.world.consumers;

import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.Liquid;
import mindustry.type.LiquidStack;

import static mindustry.Vars.content;
import static tjTool.core.TjFunc.forRange;

public class MultiStack {
    public ItemStack[] items;
    public LiquidStack[] liquids;
    public boolean[] itemFilter;
    public boolean[] liquidFilter;

    public MultiStack(ItemStack[] items, LiquidStack[] liquids) {
        this.items = items != null ? items : ItemStack.empty;
        this.liquids = liquids != null ? liquids : LiquidStack.empty;
        this.itemFilter = new boolean[content.items().size];
        this.liquidFilter = new boolean[content.liquids().size];
        for (var v : this.items) this.itemFilter[v.item.id] = true;
        for (var v : this.liquids) this.liquidFilter[v.liquid.id] = true;
    }

    public MultiStack() {
        this(null, null);
    }

    public static ItemStack[] with(Item v, Object... items) {
        return ItemStack.with(withInit(v, items));
    }

    public static LiquidStack[] with(Liquid v, Object... items) {
        return LiquidStack.with(withInit(v, items));
    }

    protected static Object[] withInit(Object v, Object... items) {
        Object[] with = new Object[items.length + 1];
        with[0] = v;
        forRange(items.length, i -> with[i + 1] = items[i]);
        return with;
    }
}
