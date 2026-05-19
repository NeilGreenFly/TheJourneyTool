package tjTool.content.blocks.sandbox;

import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.Mathf;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.*;
import arc.scene.ui.layout.Table;
import arc.util.Eachable;
import arc.util.Log;
import arc.util.io.*;
import mindustry.content.Fx;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.*;
import mindustry.graphics.Pal;
import mindustry.type.*;
import mindustry.ui.Styles;
import mindustry.world.blocks.defense.BuildTurret;
import mindustry.world.blocks.defense.turrets.BaseTurret;
import mindustry.world.blocks.payloads.BuildPayload;
import mindustry.world.blocks.payloads.UnitPayload;
import tjTool.core.*;

import static mindustry.Vars.*;
import static mindustry.ctype.ContentType.item;
import static mindustry.ctype.ContentType.liquid;
import static mindustry.graphics.Layer.overlayUI;

/**
 * 邻接源现已并入任意源, 原邻接源已被移除, 但可能会因其他测试重新加入, 不过这只会是暂时的. 
 * <p>
 * 我们仍然希望可以将弹药源也合并到这个类中, 但是由于一些基本的属性差异以及操作方式的缺失, 这个计划未能实现. 
 * 这并非一方不能兼容另一方导致的, 如果只是为了合并而合并, 我们可以让任意源具有方向, 这样即可将弹药源一起并入功能分支. 
 * 但显然绝大部分情况下 (事实上是所有情况下) 任意源和邻接源都不需要方向, 如果只是为了兼容弹药源这是没有必要的. 
 * 因此我们更需要的是一种更灵活的转换方式, 这对于各方面来说都会是必要的.
 * </p>
 * <p>
 * 另外的, 弹药源存在一些设计缺失, 比如我们或许应该阻止凭空放置弹药源 (即没有面向且邻接炮台时) , 目前该方案仍在评估, 条件允许时我们会考虑补充.
 * </p>
 */
public class AnySource extends BaseSource {
    public TextureRegion[] regions;

    public AnySource(String name) {
        super(name);
        hasLiquids = true;
        liquidCapacity = 1f;
        configurable = true;
        saveConfig = true;
        clearOnDoubleTap = true;

        config(Integer.class, (AnySourceBuild tile, Integer status) -> tile.status = status);
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(TjStat.config, table -> {
            table.row();
            for (int i = 0; i < 3; i++)
                TjStat.newConfigStats(table, regions[i],
                        TjBundle.getBlock(name, "config-name-" + i),
                        TjBundle.getBlock(name, "config-description-" + i));
        });
    }

    @Override
    public void drawPlanConfig(BuildPlan plan, Eachable<BuildPlan> list) {
        if (plan.config != null) {
            Draw.rect(regions[(int) plan.config], plan.drawx(), plan.drawy());
            Draw.reset();
        }
    }

    @Override
    public void load() {
        super.load();
        regions = new TextureRegion[] {
                atlasFind("void"),
                atlasFind("any"),
                atlasFind("adjacent"),
                atlasFind("error")
        };
    }

    @SuppressWarnings("unused")
    public class AnySourceBuild extends BaseSourceBuild {
        public int status = 1;
        private boolean handle;
        private String exception = """
                如果您重启过游戏, 在提交日志前请复现一次该异常,
                否则开发者将不会知道这是由什么导致的...
                因为在重启游戏后日志会被清空并重写, 如果您直接
                发送这个毫无意义的日志, 没有谁能够帮得了您, 先生.
                
                如果您看到了这段文本, 那么您大概率需要按照上述做一遍.
                但是不排除您可能只是重启了存档, 在日志未被清空的情况下
                您当然可以继续提交. 这是被容许的.
                """;

        @Override
        public void draw() {
            Draw.rect(status == 1 ? region : regions[status], x, y);
            Draw.reset();
            drawSelecting();
            for (var other : proximity)
                if (other instanceof BaseTurret.BaseTurretBuild && !(other instanceof BuildTurret.BuildTurretBuild)) {
                    Draw.z(overlayUI);
                    drawPlaceText(TjBundle.getBlock(name, "warning"), tile.x, tile.y, false);
                    break;
                }
        }

        @Override
        public void updateTile() {
            if (status == 1) {
                try {

                    proximity.each(this::checkBuild, other -> {
                        if (other.block.hasItems)
                            content.items().each(item -> other.handleStack(item, other.acceptStack(item, 1000000, this), this));
                        if (other.block.hasLiquids)
                            content.liquids().each(
                                    liquid -> other.acceptLiquid(this, liquid),
                                    liquid -> other.liquids.set(liquid, Math.max(other.block.liquidCapacity, other.liquids.get(liquid))));
                        if (other.block.acceptsPayload)
                            content.blocks().each(v -> {
                                var payload = new BuildPayload(v, team);
                                if (other.acceptPayload(this, payload))
                                    other.handlePayload(this, payload);
                            });
                        if (other.block.acceptsUnitPayloads)
                            content.units().each(v -> {
                                var payload = new UnitPayload(v.create(team));
                                if (other.acceptPayload(this, payload))
                                    other.handlePayload(this, payload);
                            });
                    });
                    content.items().each(item -> {
                        handle = true;
                        for (int i = 10; i-- > 0 && handle; )
                            offload(item);
                    });
                    content.liquids().each(liquid -> {
                        liquids.set(liquid, 10000f);
                        dumpLiquid(liquid);
                    });
                    liquids.clear();

                } catch (RuntimeException e) { // NullPointerException
                    exception = e.getClass().getName();
                    Log.err(e);
                    deselect();
                    status = 3;
                }
            } else if (status == 3 && Mathf.chanceDelta(0.03)) {
                Fx.regenSuppressParticle.at(x + Mathf.range(block.size * tilesize/2f - 1f), y + Mathf.range(block.size * tilesize/2f - 1f), Pal.remove);
            }
        }

        @Override
        public void buildConfiguration(Table table) {
            table.clear();
            table.background(Tex.pane).top();
            if (status == 3) {
                table.label(() -> TjDraw.flashingStream(exception + " >>>", Pal.remove, Color.valueOf("#f59f9f"))).growX().left().row();
//                table.label(() -> TjDraw.rainbowStream("NullPointerException >>>")).growX().left().row();
                table.image().color(Pal.remove).height(4).growX().padTop(5).padBottom(5).row();
                table.label(() -> """
                        这里似乎出现了异常...
                        您可以将 last_log.txt 发送给模组开发者,
                        也可以附带当前游戏内截图, 他们或许知道该怎么做.
                        
                        但请不要仅将此界面截图发送!
                        开发者需要的是日志而不是一段文本!""").color(Pal.remove).growX().left();
                return;
            }
            table.table(newTable -> {
                newTable.clear();
                newTable.background(Styles.black6).left().defaults().size(56f);
                ButtonGroup<ImageButton> group = new ButtonGroup<>();
                for (int i = 0; i < 2; i += 1) {
                    int finalI = i;
                    ImageButton button = newTable.button(
                            Tex.whiteui,
                            Styles.clearNoneTogglei,
                            36f,
                            () -> {}
                    ).tooltip(TjBundle.getBlock(name, "config-name-" + finalI)).group(group).get();
                    button.changed(() -> {
                        if (button.isChecked()) {
                            configure(finalI);
                            placeEffect.at(x, y, size);
                        }
                    });
                    button.getStyle().imageUp = new TextureRegionDrawable(regions[i]);
                    button.update(() -> button.setChecked(status == finalI));
                }
            }).row();
            table.label(() -> TjBundle.getBlock(name, "config-name-" + status)).size(120f, 40f).growX().center();
        }

        @Override
        public void handleItem(Building source, Item item) {
            handle = false;
        }

        @Override
        public Integer config() {
            return status % 3;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.i(status);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            status = read.i();
        }
    }
}
