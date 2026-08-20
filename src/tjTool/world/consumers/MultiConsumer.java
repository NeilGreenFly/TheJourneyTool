package tjTool.world.consumers;

import static mindustry.Vars.*;

public class MultiConsumer {
    public SingleConsumer[] consumers;
    public boolean[] itemFilter;
    public boolean[] liquidFilter;
    public boolean hasPower = false;
    public boolean hasHeat = false;
    public int multiplier = 4;
    public int itemCapacity = 0;
    public int[] capacities;

    public MultiConsumer(SingleConsumer... consumers) {
        this.consumers = consumers;
        this.itemFilter = new boolean[content.items().size];
        this.liquidFilter = new boolean[content.liquids().size];
        this.capacities = new int[content.items().size];
        init();
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
}
