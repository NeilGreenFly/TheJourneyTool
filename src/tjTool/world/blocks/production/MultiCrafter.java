package tjTool.world.blocks.production;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.TextureRegion;
import arc.math.Interp;
import arc.math.Mathf;
import arc.scene.actions.Actions;
import arc.scene.ui.Button;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Table;
import arc.struct.EnumSet;
import arc.util.Nullable;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.gen.*;
import mindustry.graphics.Lod;
import mindustry.graphics.Pal;
import mindustry.logic.LAccess;
import mindustry.type.Item;
import mindustry.type.Liquid;
import mindustry.ui.Bar;
import mindustry.world.Block;
import mindustry.world.blocks.heat.HeatConsumer;
import mindustry.world.draw.DrawBlock;
import mindustry.world.draw.DrawDefault;
import mindustry.world.meta.BlockFlag;
import tjTool.world.consumers.*;

import static arc.util.Strings.*;
import static mindustry.Vars.*;
import static tjTool.core.TjFunc.*;
import static tjTool.core.TjStat.multiConsumersConfig;
import static tjTool.core.TjTable.*;

public class MultiCrafter extends Block {
    public DrawBlock drawer = new DrawDefault();
    public boolean buttonDrop = false;
    public MultiConsumer multiConsumers;
    public boolean dumpExtraLiquid = true;
    public Effect craftEffect = Fx.none;
    public Effect updateEffect = Fx.none;
    public float updateEffectChance = 0.04f;
    public float updateEffectSpread = 4f;
    public float warmupSpeed = 0.019f;
    public int[] capacities;
    protected @Nullable Table consumption = null;

    public MultiCrafter(String name) {
        super(name);
        update = true;
        solid = true;
        hasItems = true;
        hasLiquids = true;
        sync = true;
        drawArrow = false;
        configurable = true;
        saveConfig = true;
        ambientSound = Sounds.loopMachine;
        ambientSoundVolume = 0.03f;
        flags = EnumSet.of(BlockFlag.factory);
        config(Integer.class, (MultiCrafterBuild build, Integer v) -> {
            build.currentConsumer = checkConsumer(v);
            build.progress = 0;
        });
    }

    protected int checkConsumer(int index) {
        return 0 <= index && index < multiConsumers.consumers.length ? index : 0;
    }

    @Override
    public void init() {
        super.init();
        itemFilter = multiConsumers.itemFilter;
        liquidFilter = multiConsumers.liquidFilter;
        hasPower = multiConsumers.hasPower;
        itemCapacity = multiConsumers.itemCapacity;
        capacities = multiConsumers.capacities;
        if (hasPower) consumePowerDynamic((MultiCrafterBuild building) -> building.currentConsumer().usage);
    }

    @Override
    public void load() {
        super.load();
        drawer.load(this);
    }

    @Override
    protected TextureRegion[] icons() {
        return drawer.finalIcons(this);
    }

    @Override
    public void setStats() { // TODO 这部分 ui 过于紧凑了, 需要使用更合适的布局方式.
        super.setStats();
        stats.add(multiConsumersConfig, multiConsumersConfig(multiConsumers));
    }

    @Override
    public void setBars() {
        super.setBars();
        removeBar("items");
        removeBar("liquid");
        if (multiConsumers.hasPower) addBar("power", entity -> new Bar(
                () -> Core.bundle.get("bar.power"),
                () -> Pal.powerBar,
                () -> Mathf.zero(consPower.requestedPower(entity)) && entity.power.graph.getPowerProduced() + entity.power.graph.getBatteryStored() > 0f ? 1f : entity.power.status)
        );
        if (multiConsumers.hasHeat) addBar("heat", (MultiCrafterBuild building) -> new Bar(
                () -> Core.bundle.format("bar.heatpercent", (int) (building.heat + 0.01f), (int) (building.efficiencyScale() * 100 + 0.01f)),
                () -> Pal.lightOrange,
                () -> building.currentConsumer().consHeat() ? building.heat / building.currentConsumer().heatRequirement : 1));
        for (var c : multiConsumers.consumers) {
            for (var stack : c.input.liquids) addLiquidBar(stack.liquid);
            for (var stack : c.output.liquids) addLiquidBar(stack.liquid);
        }
    }

    @SuppressWarnings("unused")
    public class MultiCrafterBuild extends Building implements HeatConsumer {
        public int currentConsumer = 0;
        public float[] sideHeat = new float[4];
        public float heat = 0f;
        public float progress;
        public float totalProgress;
        public float warmup;

        public SingleConsumer currentConsumer() {
            return multiConsumers.consumers[currentConsumer];
        }

        @Override
        public int getMaximumAccepted(Item item) {
            return currentConsumer().input.itemFilter[item.id] ? capacities[item.id] : 0;
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid) {
            return super.acceptLiquid(source, liquid) && currentConsumer().input.liquidFilter[liquid.id];
        }

        public void craft() {
            var cc = currentConsumer();
            cc.trigger(this);
            for (var c : cc.output.items) forRange(c.amount, i -> offload(c.item));
            if (wasVisible) craftEffect.at(x, y);
            progress %= 1f;
        }

        public void dumpOutputs() {
            var cc = currentConsumer();
            if (timer(timerDump, dumpTime / timeScale))
                for (var c : multiConsumers.consumers) for (var v : c.output.items)
                    if (!cc.input.itemFilter[v.item.id]) dump(v.item);
            for (var c : multiConsumers.consumers) for (var v : c.output.liquids)
                if (!cc.input.liquidFilter[v.liquid.id]) dumpLiquid(v.liquid, 2f, -1);
        }

        @Override
        public float progress() {
            return Mathf.clamp(progress);
        }

        @Override
        public float totalProgress() {
            return totalProgress;
        }

        @Override
        public float warmup() {
            return warmup;
        }

        @Override
        public float efficiencyScale() {
            var cc = currentConsumer();
            return cc.consHeat() ? Mathf.clamp(heat / cc.heatRequirement) : 1;
        }

        @Override
        public boolean shouldConsume() {
            var cc = currentConsumer();
            for (var v : cc.output.items) if (items.get(v.item) + v.amount > itemCapacity) return false;
            boolean allFull = cc.output.liquids.length > 0;
            for (var v : cc.output.liquids) if (liquids.get(v.liquid) < liquidCapacity - 1e-3f) allFull = false;
            else if (!dumpExtraLiquid) return false;
            if (allFull) return false;
            return super.shouldConsume() && !(cc.consHeat() && heat == 0);
        }

        @Override
        public void updateConsumption() {
            var cc = currentConsumer();
            if (cheating()) {
                potentialEfficiency = enabled && productionValid() ? 1 : 0;
                efficiency = optionalEfficiency = shouldConsume() ? potentialEfficiency : 0;
                shouldConsumePower = true;
                updateEfficiencyMultiplier();
                return;
            }
            if (!enabled) {
                potentialEfficiency = efficiency = optionalEfficiency = 0;
                shouldConsumePower = false;
                return;
            }
            efficiency = optionalEfficiency = 1;
            float e = cc.efficiency(this);
            shouldConsumePower = e > 1e-7f;
            efficiency = Math.min(cc.consPower() ? power.status : 1, e);
            potentialEfficiency = efficiency;
            boolean update = shouldConsume() && productionValid();
            if (!update) efficiency = optionalEfficiency = 0;
            updateEfficiencyMultiplier();
            if (update && this.efficiency > 0) cc.update(this);
        }

        @Override
        public void updateTile() {
            heat = calculateHeat(sideHeat);
            var cc = currentConsumer();
            if (efficiency > 0) {
                progress += getProgressIncrease(cc.craftTime);
                warmup = Mathf.approachDelta(warmup, cc.consHeat() ? Mathf.clamp(heat / cc.heatRequirement) : 1, warmupSpeed);

                // continuously output based on efficiency
                float inc = getProgressIncrease(1f);
                for (var c : cc.output.liquids)
                    handleLiquid(this, c.liquid, Math.min(c.amount * inc, liquidCapacity - liquids.get(c.liquid)));

                if (wasVisible && Mathf.chanceDelta(updateEffectChance))
                    updateEffect.at(x + Mathf.range(size * updateEffectSpread), y + Mathf.range(size * updateEffectSpread));
            } else warmup = Mathf.approachDelta(warmup, 0f, warmupSpeed);

            totalProgress += warmup * Time.delta;

            if (progress >= 1f) craft();
            dumpOutputs();
        }

        @Override
        public void buildConfiguration(Table table) {
            var image = new Image(Tex.pane) { int c; };
            table.background(Tex.paneLeft);
            table.stack(
                    new Table(t -> t.add(image).growX().height(uiSize).padTop(!buttonDrop ? uiSize * currentConsumer : 0)).top(),
                    new Table(t -> {
                        t.marginLeft(10).marginRight(10);
                        for (var consumer : multiConsumers.consumers) {
                            t.table(input -> {
                                input.left();
                                for (var v : consumer.input.items) stack(input, v);
                                for (var v : consumer.input.liquids) stack(input, v);
                                if (consumer.consHeat()) stack(input, new Image(Icon.waves), String.valueOf((int) consumer.heatRequirement), Pal.remove);
                            }).growX();
                            t.image(Icon.rightOpen).padLeft(10).padRight(10);
                            t.table(output -> {
                                output.right();
                                for (var v : consumer.output.items) stack(output, v);
                                for (var v : consumer.output.liquids) stack(output, v);
                            }).growX().row();
                        }
                    }),
                    new Table(t -> forEach(multiConsumers.consumers, (i, consumer) -> {
                        var button = new Button(style);
                        button.clicked(() -> {
                            if (currentConsumer != i) configure(i);
                            if (consumption != null) displayConsumption(consumption);
                        });
                        button.update(() -> {
                            if (!(currentConsumer == i && image.c != i)) return;
                            image.c = i;
                            image.clearActions();
                            image.actions(Actions.moveTo(button.x, button.y, 0.2f, Interp.fastSlow));
                        });
                        t.add(button).tooltip(tip -> {
                            tip.background(Tex.paneLeft).marginLeft(20);
                            tip.table(input -> {
                                input.left();
                                for (var v : consumer.input.items) stack(input, v);
                                for (var v : consumer.input.liquids) stack(input, v);
                                if (consumer.consPower()) stack(input, new Image(Icon.power), autoFixed(consumer.usage * 60f, 3) + "[gray]/s[]", Pal.accent);
                                if (consumer.consHeat()) stack(input, new Image(Icon.waves), String.valueOf((int) consumer.heatRequirement), Pal.remove);
                            }).growX();
                            tip.image(Icon.rightOpen).padLeft(10).padRight(10);
                            tip.add(autoFixed(consumer.craftTime / 60f, 3) + "[gray]s[]");
                            tip.image(Icon.rightOpen).padLeft(10).padRight(10);
                            tip.table(output -> {
                                output.right();
                                for (var v : consumer.output.items) stack(output, v);
                                for (var v : consumer.output.liquids) stack(output, v);
                            }).growX();
                        }).growX().height(uiSize).row();
                    }))
            );
        }

        @Override
        public boolean onConfigureBuildTapped(Building other) {
            if (this == other) deselect();
            return this != other;
        }

        @Override
        public void displayConsumption(Table table) {
            consumption = table;
            table.clear();
            currentConsumer().displayConsumption(table.left(), this);
        }

        @Override
        public void draw() {
            drawer.draw(this);
            if (renderer.drawStatus) drawStatus();
        }

        @Override
        public void drawStatus() {
            float multiplier = block.size > 1 ? 1 : 0.64f;
            float cx = x + (block.size * 8) / 2f - 8f * multiplier / 2f;
            float cy = y - (block.size * 8) / 2f + 8f * multiplier / 2f;
            Draw.z(71f);
            Draw.color(Pal.gray, Lod.alpha2);
            Fill.square(cx, cy, 2.5f * multiplier, 45f);
            Draw.color(status().color, Lod.alpha2);
            Fill.square(cx, cy, 1.5f * multiplier, 45f);
            Draw.color();
        }

        @Override
        public float[] sideHeat() {
            return sideHeat;
        }

        @Override
        public float heatRequirement() {
            return currentConsumer().heatRequirement;
        }

        @Override
        public Object config() {
            return currentConsumer;
        }

        @Override
        public void control(LAccess type, double p1, double p2, double p3, double p4) {
            if (type == LAccess.config) configured(null, (int) p1);
            else super.control(type, p1, p2, p3, p4);
        }

        @Override
        public double sense(LAccess sensor) {
            if (sensor == LAccess.heat) return heat;
            return super.sense(sensor);
        }

        @Override
        public Object senseObject(LAccess sensor) {
            // TODO How to return a int number?
            if (sensor == LAccess.config) return content.item(currentConsumer);
            return super.senseObject(sensor);
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.b(currentConsumer);
            write.f(progress);
            write.f(warmup);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            currentConsumer = checkConsumer(read.b());
            progress = read.f();
            warmup = read.f();
        }
    }
}
