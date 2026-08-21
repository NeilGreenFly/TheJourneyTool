package tjTool.world.consumers;

import arc.func.Intc;
import mindustry.gen.Building;
import mindustry.type.Liquid;

import static mindustry.Vars.*;

public class MultiConsumer {
    public SingleConsumer[] consumers;
    public OptionalConsumer[] optionalConsumers;
    public boolean[] itemFilter;
    public boolean[] liquidFilter;
    public boolean[] optionalItemFilter;
    public boolean[] optionalLiquidFilter;
    public boolean hasPower = false;
    public boolean hasHeat = false;
    public int multiplier = 4;
    public int itemCapacity = 0;
    public int[] capacities;

    public MultiConsumer(SingleConsumer... consumers) {
        this.consumers = consumers;
        this.optionalConsumers = new OptionalConsumer[0];
        this.itemFilter = new boolean[content.items().size];
        this.liquidFilter = new boolean[content.liquids().size];
        this.optionalItemFilter = new boolean[content.items().size];
        this.optionalLiquidFilter = new boolean[content.liquids().size];
        this.capacities = new int[content.items().size];
        init();
    }

    public MultiConsumer optional(OptionalConsumer... consumers) {
        this.optionalConsumers = consumers;
        for (var consumer : optionalConsumers) {
            for (var stack : consumer.input.liquids) optionalLiquidFilter[stack.liquid.id] = true;
            for (var stack : consumer.input.items) optionalItemFilter[stack.item.id] = true;
        }
        return this;
    }

    @SuppressWarnings("unused")
    public void multi(int multiplier) {
        this.multiplier = multiplier;
    }

    protected void init() {
        for (var consumer : consumers) {
            if (consumer.consPower()) hasPower = true;
            if (consumer.consHeat()) hasHeat = true;
            for (var stack : consumer.input.liquids) liquidFilter[stack.liquid.id] = true;
            for (var stack : consumer.input.items) {
                itemFilter[stack.item.id] = true;
                int amount = stack.amount * multiplier;
                if (capacities[stack.item.id] < amount) capacities[stack.item.id] = amount;
                if (itemCapacity < amount) itemCapacity = amount;
            }
        }
    }

    public boolean hasOption() {
        return optionalConsumers.length > 0;
    }

    public boolean acceptLiquid(int cc, Liquid liquid) {
        return consumers[cc].input.liquidFilter[liquid.id] || optionalLiquidFilter[liquid.id];
    }

    public void efficiency(Building building, int cc, Intc setOption) {
        var consumer = consumers[cc];
        float efficiency = consumer.efficiency(building);
        building.shouldConsumePower = efficiency > 1e-7f;
        building.efficiency = Math.min(consumer.consPower() ? building.power.status : 1, efficiency) * optionalEfficiency(building, setOption);
    }

    public float optionalEfficiency(Building building, Intc setOption) {
        if (!hasOption()) return 1;
        int option = -1;
        float max = 0;
        for (int i = 0; i < optionalConsumers.length; i += 1) {
            float e = optionalConsumers[i].efficiency(building);
            if (max < e) {
                max = e;
                option = i;
            }
        }
        if (max == 0) {
            setOption.get(-1);
            return 1;
        }
        setOption.get(option);
        return max;
    }

    public void update(int cc, int option, Building building) {
        consumers[cc].update(building);
        if (option != -1) optionalConsumers[option].update(building);
    }

    public void trigger(int cc, int option, Building building) {
        consumers[cc].trigger(building);
        if (option != -1) optionalConsumers[option].trigger(building);
    }
}
