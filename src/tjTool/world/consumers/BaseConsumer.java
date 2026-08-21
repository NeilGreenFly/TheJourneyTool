package tjTool.world.consumers;

import mindustry.gen.Building;

public class BaseConsumer {
    public MultiStack input;

    public float efficiency(Building building) {
        if (!(building.consumeTriggerValid() || building.items.has(input.items))) return 0;
        if (!(building.shouldConsume() && building.productionValid())) return 0;
        float delta = building.delta() * building.efficiencyScale();
        if (delta <= 1e-8f) return 0;
        float min = 1;
        for (var stack : input.liquids) min = Math.min(building.liquids.get(stack.liquid) / (stack.amount * delta), min);
        return min;
    }

    public void update(Building building) {
        for (var v : input.liquids) building.liquids.remove(v.liquid, v.amount * building.edelta());
    }

    public void trigger(Building building) {
        for (var v : input.items) building.items.remove(v.item, v.amount);
    }
}
