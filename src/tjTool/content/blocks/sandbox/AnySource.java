package tjTool.content.blocks.sandbox;

import arc.graphics.g2d.*;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.*;
import arc.scene.ui.layout.Table;
import arc.util.Eachable;
import arc.util.io.*;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.ui.Styles;
import mindustry.world.blocks.defense.turrets.BaseTurret;
import mindustry.world.meta.BuildVisibility;

import static mindustry.Vars.*;
import static mindustry.graphics.Layer.overlayUI;

/**
 * 邻接源现已并入任意源, 原邻接源已被移除, 但可能会因其他测试重新加入, 不过这只会是暂时的. 
 * <p></p>
 * 我们仍然希望可以将弹药源也合并到这个类中, 但是由于一些基本的属性差异以及操作方式的缺失, 这个计划未能实现. 
 * 这并非一方不能兼容另一方导致的, 如果只是为了合并而合并, 我们可以让任意源具有方向, 这样即可将弹药源一起并入功能分支. 
 * 但显然绝大部分情况下 (事实上是所有情况下) 任意源和邻接源都不需要方向, 如果只是为了兼容弹药源这是没有必要的. 
 * 因此我们更需要的是一种更灵活的转换方式, 这对于各方面来说都会是必要的. 
 * <p></p>
 * 另外的, 弹药源存在一些设计缺失, 比如我们或许应该阻止凭空放置弹药源 (即没有面向且邻接炮台时) , 目前该方案仍在评估, 条件允许时我们会考虑补充. 
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
                atlasFind("adjacent")
        };
    }

    public class AnySourceBuild extends BaseSourceBuild {
        public int status = 0;

        @Override
        public boolean checkBuild(Building other) {
            return super.checkBuild(other)
                    && other.block.buildVisibility != BuildVisibility.sandboxOnly
                    && other.block.category != Category.distribution;
        }

        @Override
        public void draw() {
            Draw.rect(regions[status], x, y);
            Draw.reset();
            drawSelecting();
            for (var other: proximity)
                if (other instanceof BaseTurret.BaseTurretBuild) {
                    Draw.z(overlayUI);
                    drawPlaceText("! 请不要将任意源与炮台相邻放置 !", tile.x, tile.y, false);
                    break;
                }
        }

        @Override
        public void updateTile() {

            switch (status) {

                case 1:
                    for (Item item: content.items())
                        for (int i = 10; i --> 0;)
                            offload(item);
                    for (Liquid liquid: content.liquids()) {
                        liquids.set(liquid, 10000f);
                        dumpLiquid(liquid);
                    }
                    liquids.clear();
                    // TODO UnitAssembler
                    break;

                case 2:
                    for (var other : proximity) {
                        if (checkBuild(other)) {
                            if (other.block.category == Category.turret) {
                                continue;
                            }
                            if (other.block.hasItems)
                                for (Item item : content.items())
                                    if (other.acceptItem(this, item))
                                        other.items.set(item, Math.max(other.getMaximumAccepted(item), other.items.get(item)));
                            if (other.block.hasLiquids)
                                for (Liquid liquid : content.liquids())
                                    if (other.acceptLiquid(this, liquid))
                                        other.liquids.set(liquid, Math.max(other.block.liquidCapacity, other.liquids.get(liquid)));
                        }
                    }
                    break;

                default: break;
            }
        }

        @Override
        public void buildConfiguration(Table table) {
            String[] names = {"空", "任意", "邻接"};
            table.clear();
            table.background(Tex.pane).top();
            table.table(newTable -> {
                newTable.clear();
                newTable.background(Styles.black6).left().defaults().size(56f);
                ButtonGroup<ImageButton> group = new ButtonGroup<>();
                for (int i = 0; i < regions.length; i += 1) {
                    int finalI = i;
                    ImageButton button = newTable.button(
                            Tex.whiteui,
                            Styles.clearNoneTogglei,
                            36f,
                            () -> {}
                    ).tooltip(names[i] + "源").group(group).get();
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
            table.label(() -> names[status] + "源").size(120f, 40f);
        }

        @Override
        public Integer config() {
            return status;
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
