package tjTool.world.blocks.sandbox;

import arc.Core;
import arc.func.Boolc;
import arc.func.Boolp;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.ImageButton;
import arc.scene.ui.layout.Table;
import arc.util.Eachable;
import arc.util.Nullable;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.content.Blocks;
import mindustry.content.Fx;
import mindustry.entities.Units;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.io.TypeIO;
import mindustry.type.Item;
import mindustry.type.UnitType;
import mindustry.ui.Styles;
import mindustry.world.Block;
import mindustry.world.blocks.defense.BuildTurret;
import mindustry.world.blocks.defense.turrets.BaseTurret;
import mindustry.world.meta.BlockGroup;
import tjTool.core.*;

import java.util.Arrays;

import static mindustry.Vars.*;
import static mindustry.graphics.Layer.*;
import static tjTool.core.TjTable.*;
import static tjTool.core.TjVars.halfSize;

/**
 * 邻接源现已并入任意源, 原邻接源已被移除, 但可能会因其他测试重新加入, 不过这只会是暂时的.<p>
 * 我们仍然希望可以将弹药源也合并到这个类中, 但是由于一些基本的属性差异以及操作方式的缺失, 这个计划未能实现. 
 * 这并非一方不能兼容另一方导致的, 如果只是为了合并而合并, 我们可以让任意源具有方向, 这样即可将弹药源一起并入功能分支. 
 * 但显然绝大部分情况下 (事实上是所有情况下) 任意源和邻接源都不需要方向, 如果只是为了兼容弹药源这是没有必要的. 
 * 因此我们更需要的是一种更灵活的转换方式, 这对于各方面来说都会是必要的.<p>
 * 另外的, 弹药源存在一些设计缺失, 比如我们或许应该阻止凭空放置弹药源 (即没有面向且邻接炮台时) , 目前该方案仍在评估, 条件允许时我们会考虑补充.<p>
 * 不过如今我们决定将弹药源更名为定向源, 并进一步完善其功能, 使其逐渐成为任意源的下位替代, 这或许会是一个还算不错的分支.
 */
public class AnySource extends BaseSource {
    public TextureRegion regionDisabled;
    public TextureRegion regionError;

    public AnySource(String name) {
        super(name);
        solid = false;
        hasLiquids = true;
        liquidCapacity = 1f;
        configurable = true;
        saveConfig = true;
        clearOnDoubleTap = true;
        commandable = true;
    }

    @Override
    protected void config() {
        config(boolean[].class, (AnySourceBuild build, boolean[] v) -> {
            build.outputItem = v[0];
            build.outputLiquid = v[1];
            build.outputPower = v[2];
            build.outputHeat = v[3];
            build.outputPayload = v[4];
        });
        config(Integer.class, (AnySourceBuild build, Integer v) -> {
            if (v == 2) build.commandPos = null;
        });
        config(UnitType.class, (AnySourceBuild build, UnitType v) -> {
            lastConfig = null;
            if (v.useUnitCap && build.team.data().countType(v) >= Units.getCap(build.team)) {
                Fx.unitCapKill.at(build);
                return;
            }
            Fx.spawn.at(build);
            if (net.client()) return;
            var u = v.spawn(build.team, build.x, build.y, 90);
            if (build.commandPos != null && u.isCommandable()) u.command().commandPosition(build.commandPos);
        });
    }

    @Override
    public void setStats() {
        super.setStats();
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) {
        if (plan.config instanceof boolean[] config)
            Draw.rect(config[0] || config[1] || config[2] || config[3] || config[4] ? region : regionDisabled, plan.drawx(), plan.drawy());
        else Draw.rect(region, plan.drawx(), plan.drawy());
    }

    @Override
    public void load() {
        super.load();
        regionDisabled = atlasFind("void");
        regionError = atlasFind("error");
    }

    @Override
    public boolean outputsItems() {
        return true;
    }

    @Override
    public boolean canReplace(Block other) {
        return other.alwaysReplace || !other.privileged && other.replaceable && (other != this || (rotate && quickRotate)) &&
                ((group != BlockGroup.none && (other.group == BlockGroup.transportation || other.group == BlockGroup.liquids)) || other == this) &&
                (size == other.size || (size >= other.size && ((subclass != null && subclass == other.subclass) || group.anyReplace)));
    }

    @SuppressWarnings("unused")
    public class AnySourceBuild extends BaseSourceBuild {
        public boolean outputItem = true;
        public boolean outputLiquid = true;
        public boolean outputPower = true;
        public boolean outputPayload = true;
        public boolean outputHeat = true;
        public float outputOverdrive;

        public @Nullable Vec2 commandPos;
        private boolean handle;
        private boolean warning;
        public boolean error;
        private String exception = """
                如果您重启过游戏, 在提交日志前请复现一次该异常,
                否则开发者将不会知道这是由什么导致的...
                因为在重启游戏后日志会被清空并重写, 如果您直接
                发送这个毫无意义的日志, 没有谁能够帮得了您, 先生.
                
                如果您看到了这段文本, 那么您大概率需要按照上述做一遍.
                但是不排除您可能只是重启了存档, 在日志未被清空的情况下
                您当然可以继续提交. 这是被容许的.
                """;
        public Layout layout = new Layout(this).with(
                new Page(Icon.wrench).with(new EmptyContent<>(table -> {
                    addButton(table, Icon.box, () -> outputItem, v -> outputItem = v, Blocks.itemSource);
                    addButton(table, Icon.liquid, () -> outputLiquid, v -> outputLiquid = v, Blocks.liquidSource);
                    addButton(table, Icon.power, () -> outputPower, v -> outputPower = v, Blocks.powerSource);
                    addButton(table, Icon.waves, () -> outputHeat, v -> outputHeat = v, Blocks.heatSource);
                    addButton(table, Icon.units, () -> outputPayload, v -> outputPayload = v, Blocks.payloadSource);
                    table.button(Icon.logic, Styles.clearNonei, () -> {
                        boolean[] config = (boolean[]) config();
                        Arrays.fill(config, !getStatus());
                        configure(config);
                    }).size(uiSize).tooltip("@addall", true).pad(5);
                    table.row();
                    table.button(Icon.trash, iconSize, () -> configure(2)).size(uiSize).tooltip("@stance.stop", true).padTop(100);
                })),
                new Page(Icon.units).with(
                        new EmptyContent<>(table -> table.background(Styles.black6).label(() -> "Create a unit above").height(uiSize)),
                        Selection.unlockableContent(content.units().select(BaseSource::canProduce)::as, () -> null))
        );

        private void addButton(Table table, TextureRegionDrawable icon, Boolp isChecked, Boolc setChecked, Block block) {
            table.add(new ImageButton(icon, Styles.clearNoneTogglei) {{
                resizeImage(iconSize);
                update(() -> setChecked(isChecked.get()));
                changed(() -> {
                    setChecked.get(isChecked());
                    configure(config());
                });
            }}).size(uiSize).tooltip(tip -> {
                tip.background(Tex.paneLeft).image(block.uiIcon).size(iconSize).padLeft(5).padRight(10);
                tip.label(() -> block.localizedName + "    " + Core.bundle.get(isChecked.get() ? "mod.enabled" : "mod.disabled") + "[]\n" + block.description);
            }).pad(5);
        }

        public boolean getStatus() {
            return enabled && (outputItem || outputLiquid || outputPower || outputHeat || outputPayload);
        }

        @Override
        public void draw() {
            Draw.rect(!error ? getStatus() ? region : regionDisabled : regionError, x, y);
            Draw.reset();
            drawSelecting();
            if (warning) {
                Draw.z(overlayUI);
                drawPlaceText(TjBundle.getBlock(name, "warning"), tile, false);
            }
        }

        @Override
        public void onProximityUpdate() {
            warning = proximity.contains(other -> other instanceof BaseTurret.BaseTurretBuild && !(other instanceof BuildTurret.BuildTurretBuild));
        }

        @Override
        public void updateTile() {
            if (!error && getStatus()) super.updateTile();
            else if (error && Mathf.chanceDelta(0.03)) {
                Fx.regenSuppressParticle.at(x + Mathf.range(block.size * halfSize - 1f), y + Mathf.range(block.size * tilesize / 2f - 1f), Pal.remove);
            }
        }

        @Override
        public void tryUpdateTile() {
            if (outputItem) content.items().each(item -> {
                handle = true;
                for (int i = 10; i-- > 0 && handle;) offload(item);
            });
            if (outputLiquid) content.liquids().each(liquid -> {
                liquids.set(liquid, 1000000f);
                dumpLiquid(liquid);
            });
            liquids.clear();
            proximity.each(this::checkBuild, other -> {
                if (outputItem && other.block.hasItems) content.items().each(
                        item -> other.acceptItem(this, item),
                        item -> other.handleStack(item, other.acceptStack(item, 1000000, this), this));
                // 这部分通常是不安全的, 以及液体的交互方式与物品略有不同, 因此这个处理并不重要.
                // if (outputLiquid && other.block.hasLiquids) content.liquids().each(
                //         liquid -> other.acceptLiquid(this, liquid),
                //         liquid -> other.liquids.set(liquid, Math.max(other.block.liquidCapacity, other.liquids.get(liquid))));
                if (!outputPayload) return;
                if (other.block.acceptsPayload) content.blocks().each(BaseSource::canProduce,
                        v -> payloadPool(v, team, BaseSource.dumpPayload(this, other)));
                if (other.block.acceptsUnitPayloads) content.units().each(BaseSource::canProduce,
                        v -> payloadPool(v, team, BaseSource.dumpPayload(this, other)));
            });
        }

        @Override
        protected void catchUpdateTile(Exception e) {
            exception = e.getClass().getName();
            error = true;
            configured(null, config());
        }

        @Override
        public float getPowerProduction() {
            return outputPower ? super.getPowerProduction() : 0;
        }

        @Override
        public float heat() {
            return outputHeat ? super.heat() : 0;
        }

        @Override
        public void buildConfiguration(Table table) {
            if (!error) {
                table.table(Tex.pane, frame -> layout.build(block, frame, false));
                return;
            }
            table.background(Tex.pane);
            table.label(() -> TjDraw.flashingStream(exception + " >>>", Pal.remove, Color.valueOf(Tmp.c1, "#f59f9f"))).growX().left().row();
            table.image().color(Pal.remove).height(4).growX().padTop(5).padBottom(5).row();
            table.label(() -> """
                    这里似乎出现了异常...
                    您可以将 last_log.txt 发送给模组开发者,
                    也可以附带当前游戏内截图, 他们或许知道该怎么做.

                    但请不要仅将此界面截图发送!
                    开发者需要的是日志而不是一段文本!""").color(Pal.remove).growX().left();
        }

        @Override
        public void onCommand(Vec2 target) {
            commandPos = target;
        }

        @Override
        public Vec2 getCommandPosition() {
            return commandPos;
        }

        @Override
        public void handleItem(Building source, Item item) {
            handle = false;
        }

        @Override
        public Object config() {
            if (error) return new boolean[5];
            return new boolean[]{
                    outputItem,
                    outputLiquid,
                    outputPower,
                    outputHeat,
                    outputPayload};
        }

        @Override
        public byte version() {
            return 2;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.bool(outputItem);
            write.bool(outputLiquid);
            write.bool(outputPower);
            write.bool(outputHeat);
            write.bool(outputPayload);
            TypeIO.writeVecNullable(write, commandPos);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            if (revision == 0) {
                read.i(); // status
            } else if (revision == 1) {
                read.i(); // status
                commandPos = TypeIO.readVecNullable(read);
            } else if (revision == 2) {
                outputItem = read.bool();
                outputLiquid = read.bool();
                outputPower = read.bool();
                outputHeat = read.bool();
                outputPayload = read.bool();
                commandPos = TypeIO.readVecNullable(read);
            }
        }
    }
}
