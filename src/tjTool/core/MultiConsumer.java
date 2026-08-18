package tjTool.core;

import mindustry.gen.Building;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;

import static mindustry.Vars.content;

public class MultiConsumer {
    public float craftTime = 60;
    public MultiConsumer.MultiStack input;
    public MultiConsumer.MultiStack output;
    public float usage = 0;
    public float heatRequirement = 0;

    private MultiConsumer(MultiConsumer.MultiStack input, MultiConsumer.MultiStack output) {
        this.input = input;
        this.output = output;
    }

    public static MultiConsumer by(ItemStack[] inputItems, LiquidStack[] inputLiquids, ItemStack[] outputItems, LiquidStack[] outputLiquids) {
        return new MultiConsumer(new MultiConsumer.MultiStack(inputItems, inputLiquids), new MultiConsumer.MultiStack(outputItems, outputLiquids));
    }

    public MultiConsumer time(float craftTime) {
        this.craftTime = craftTime;
        return this;
    }

    public MultiConsumer timePerSec(float sec) {
        return time(sec * 60f);
    }

    public MultiConsumer power(float usage) {
        this.usage = usage;
        return this;
    }

    public MultiConsumer powerPerSec(int usage) {
        return power(usage / 60f);
    }

    public boolean consPower() {
        return usage > 0;
    }

    public MultiConsumer heat(float heat) {
        this.heatRequirement = heat;
        return this;
    }

    public boolean consHeat() {
        return heatRequirement > 0;
    }

    public float efficiency(Building building) {
        if (!(building.consumeTriggerValid() || building.items.has(input.items, 1))) return 0;
        float delta = building.edelta() * building.efficiencyScale();
        if (delta <= 1e-8f) return 0f;
        float min = 1f;
        for (var stack : input.liquids) min = Math.min(building.liquids.get(stack.liquid) / (stack.amount * delta), min);
        return min;
    }

    public void update(Building building) {
        for (var v : input.liquids) building.liquids.remove(v.liquid, v.amount * building.edelta());
    }

    public void trigger(Building building) {
        for (var v : input.items) building.items.remove(v.item, v.amount);
    }

    public static class MultiStack {
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
    }
}
