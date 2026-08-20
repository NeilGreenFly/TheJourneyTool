package tjTool.world.consumers;

import arc.scene.ui.layout.Table;
import mindustry.gen.Building;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.ui.ReqImage;

import static mindustry.Vars.*;
import static mindustry.world.meta.StatValues.stack;

public class SingleConsumer {
    public float craftTime = 60;
    public MultiStack input;
    public MultiStack output;
    public float usage = 0;
    public float heatRequirement = 0;
    // TODO liquidOutputDirections

    private SingleConsumer(MultiStack input, MultiStack output) {
        this.input = input;
        this.output = output;
    }

    public static SingleConsumer by(float sec) {
        return byTick(sec * 60f);
    }

    public static SingleConsumer byTick(float craftTime) {
        var singleConsumer = new SingleConsumer(new MultiStack(), new MultiStack());
        singleConsumer.craftTime = craftTime;
        return singleConsumer;
    }

    public SingleConsumer inputBy(ItemStack[] items, LiquidStack[] liquids) {
        input = new MultiStack(items, liquids);
        return this;
    }

    public SingleConsumer outputBy(ItemStack[] items, LiquidStack[] liquids) {
        output = new MultiStack(items, liquids);
        return this;
    }

    public SingleConsumer powerPerSec(int usage) {
        return power(usage / 60f);
    }

    public SingleConsumer power(float usage) {
        this.usage = usage;
        return this;
    }

    public SingleConsumer heat(float heat) {
        this.heatRequirement = heat;
        return this;
    }

    public boolean consPower() {
        return usage > 0;
    }

    public boolean consHeat() {
        return heatRequirement > 0;
    }

    public float efficiency(Building building) {
        if (!(building.consumeTriggerValid() || building.items.has(input.items, 1))) return 0;
        float delta = building.edelta() * building.efficiencyScale();
        if (delta <= 1e-8f) return 0f;
        float min = 1f;
        for (var stack : input.liquids)
            min = Math.min(building.liquids.get(stack.liquid) / (stack.amount * delta), min);
        return min;
    }

    public void update(Building building) {
        for (var v : input.liquids) building.liquids.remove(v.liquid, v.amount * building.edelta());
    }

    public void trigger(Building building) {
        for (var v : input.items) building.items.remove(v.item, v.amount);
    }

    public void displayConsumption(Table table, Building building) {
        table.table(c -> {
            c.defaults().padRight(8);
            for (var v : input.items)
                c.add(new ReqImage(stack(v.item, v.amount), () -> building.items.has(v.item, v.amount)));
            for (var v : input.liquids)
                c.add(new ReqImage(v.liquid.uiIcon, () -> building.liquids.get(v.liquid) > 0)).size(iconMed);
        });
    }
}
