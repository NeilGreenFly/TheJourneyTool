package tjTool.content.blocks.sandbox;

import arc.Core;
import arc.Events;
import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.scene.ui.*;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.io.*;
import mindustry.content.Blocks;
import mindustry.ctype.UnlockableContent;
import mindustry.game.EventType;
import mindustry.gen.*;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.*;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Block;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.draw.*;
import tjTool.core.*;

import static mindustry.Vars.*;
import static tjTool.core.TjConfigTable.*;
import static tjTool.core.TjTable.*;

public class AmmoSource extends BaseSource {
    public boolean shown = true;

    public AmmoSource(String name) {
        super(name);
        rotate = true;
        quickRotate = false;
        regionRotated1 = 1;
        configurable = true;
        saveConfig = true;
        clearOnDoubleTap = true;
        drawer = new DrawMulti(
                new DrawDefault(),
                new DrawHeatOutput()
        );
        config(int[].class, (AmmoSourceBuild tile, int[] v) -> tile.pack.receive(v));
        config(Block.class, (AmmoSourceBuild tile, Block v) -> {
            lastConfig = null;
            if (tile.turretBuild == null) return;
            float rotation = tile.turretBuild.rotation;
            tile.turretBuild.tile.setBlock(v, tile.team);
            tile.turretBuild.rotation = rotation;
            tile.turretBuild.block.placeEffect.at(tile.turretBuild, tile.turretBuild.block.size);
        });
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(TjStat.config, table -> {
            table.row();
            TjStat.newConfigStats(table, Icon.turret.getRegion(), TjBundle.getBlock(name, "config-ammo"), TjBundle.getBlock(name, "config-ammo-description"));
            TjStat.newConfigStats(table, Icon.star.getRegion(), TjBundle.getBlock(name, "config-boost"), TjBundle.getBlock(name, "config-boost-description"));
            TjStat.newConfigStats(table, Icon.download.getRegion(), TjBundle.getBlock(name, "config-consumes"), TjBundle.getBlock(name, "config-consumes-description"));
            TjStat.newConfigStats(table, Icon.effect.getRegion(), TjBundle.getBlock(name, "config-overdrive"), TjBundle.getBlock(name, "config-overdrive-description"));
        });
    }

    @SuppressWarnings("unused")
    public class AmmoSourceBuild extends BaseSourceBuild {
        public @Nullable BaseTurret.BaseTurretBuild turretBuild = null;
        public ConfigPack pack = new ConfigPack(this).with(
                new ConfigContent(),
                new ConfigContent(new Image(Icon.star), TjBundle.getBlock(name, "config-boost")).setAlwaysBuild(false),
                new ConfigContent(new Image(Icon.download), TjBundle.getBlock(name, "config-consumes")).setAlwaysBuild(false).setLock(true),
                new ConfigContent(new Image(Icon.effect), TjBundle.getBlock(name, "config-overdrive"), Seq.with(
                        new Option(Blocks.overdriveProjector.uiIcon, "150%"),
                        new Option(Blocks.overdriveProjector.uiIcon, "225%"),
                        new Option(Blocks.overdriveDome.uiIcon, "250%")
                )).setFavorite(2).setAlwaysBuild(false).setStatic(true)
        );
        public Layout layout = new Layout(this::configure).with(
                new Page(Icon.wrench).with(Selection.unlockableContent(() -> content.blocks().select(
                        block -> block instanceof BaseTurret && block.size == turretBuild.block.size).as(), () -> turretBuild != null ? turretBuild.block : null)),
                new Page(Icon.link).with(new Content<>() {
                    private final BaseDialog dialog = new BaseDialog("@openlink");

                    {
                        dialog.cont.label(() -> "即将打开链接").center().row();
                        dialog.cont.button("QQ Group: 1078329722", Icon.link, Styles.grayt,
                                () -> Core.app.openURI("https://qm.qq.com/q/ZfZKpzUzaU"))
                                .tooltip("@openlink", true).margin(8f).size(300f, 50f).pad(10f).row();
                        dialog.cont.label(() -> "由于拦截不良账号的需要，在入群申请中请输入答案: [sky]DeepSpace[]").padTop(50f).center().row();
                        dialog.cont.label(() -> "不仅仅是BUG, 如果您有新的[accent]需求[]或[accent]想法[], 也可以向我们提交它们!").padTop(50f).center().row();
                        dialog.addCloseButton();
                    }

                    @Override
                    public Cons<Table> build(boolean closeSelect) {
                        return table -> {
                            table.label(() -> "遇到了[accent]BUG[]? 向我们提交它们!").padTop(20f).center().row();
                            table.button("Submit", Icon.link, Styles.grayt, dialog::show)
                                    .tooltip("@openlink", true).margin(8f).size(300f, 50f).pad(10f);
                        };
                    }

                    @Override
                    protected void call(int config) {}

                    @Override
                    protected int getConfig() {
                        return 0;
                    }
                })
        );
        public Seq<UnlockableContent> consumes = new Seq<>();
        public Seq<UnlockableContent> ammoTypes = new Seq<>();
        public Seq<Liquid> coolant = new Seq<>();
        public float[] overdrives = {1.5f, 2.25f, 2.5f};
        public float heat;

        public UnlockableContent getAmmo() {
            return pack.from(ammoTypes, 0);
        }

        public Liquid getCool() {
            return pack.from(coolant, 1);
        }

        public void drawItemSelections(Building building, Seq<UnlockableContent> selections) {
            float dx = building.x - (building.block.size * 8) / 2f;
            float dy = building.y + (building.block.size * 8) / 2f;
            for (var selection : selections)
                if (selection != null) {
                    float s = 6f * selection.fullIcon.ratio();
                    float h = 6f;
                    Draw.mixcol(Color.darkGray, 1f);
                    Draw.rect(selection.fullIcon, dx, dy - 1f, s, h);
                    Draw.reset();
                    Draw.rect(selection.fullIcon, dx, dy, s, h);
                    dy -= 7f;
                }
        }

        @Override
        public void draw() {
            super.draw();
            Draw.z(Layer.blockAdditive);
            TjDraw.overdrive(this, "#D2F0FF", "#CBA3FF", heat);
            if (checkBuild(turretBuild))
                TjDraw.overdrive(turretBuild, "#ffd59e", heat);
        }

        @Override
        public void drawSelect() {
            super.drawSelect();
            if (turretBuild != null) {
                TjDraw.drawPlace(turretBuild.block, turretBuild.tile.x, turretBuild.tile.y, true);
                drawItemSelection(turretBuild.block);
                drawItemSelections(turretBuild, Seq.with(getAmmo(), getCool()));
            }
        }

        @Override
        public void drawConfigure() {
            super.drawConfigure();
            drawSelect();
        }

        @Override
        public void onProximityUpdate() {
            pack.clear();
            consumes.clear();
            ammoTypes.clear();
            coolant.clear();
            Building b = front();
            if (checkBuild(b) && b instanceof BaseTurret.BaseTurretBuild baseTurretBuild && (turretBuild == baseTurretBuild || turretBuild == null)) {
                turretBuild = baseTurretBuild;

                if (turretBuild.block.hasItems)
                    consumes.addAll(content.items().select(turretBuild.block::consumesItem));
                if (turretBuild.block.hasLiquids)
                    consumes.addAll(content.liquids().select(turretBuild.block::consumesLiquid));

                if (turretBuild instanceof ItemTurret.ItemTurretBuild)
                    ammoTypes.addAll(consumes.select(Item.class::isInstance));
                else if (turretBuild instanceof LiquidTurret.LiquidTurretBuild || turretBuild instanceof ContinuousLiquidTurret.ContinuousLiquidTurretBuild)
                    ammoTypes.addAll(consumes.select(Liquid.class::isInstance));
                ammoTypes.map(consumes::remove);

                var turretCoolant = ((BaseTurret) turretBuild.block).coolant;
                if (turretCoolant != null)
                    content.liquids().each(
                            turretCoolant::consumes,
                            liquid -> {
                                coolant.add(liquid);
                                consumes.remove(liquid);
                            });

                pack.get(0).setIcon(new Image(turretBuild.block.uiIcon)).setTip(turretBuild.block.localizedName).setOptions(ammoTypes.map(optionMapper));
                pack.get(1).setOptions(coolant.map(
                        turretBuild instanceof ReloadTurret.ReloadTurretBuild reloadTurretBuild
                        ? liquid -> new Option(liquid.uiIcon, liquid.emoji() + liquid.localizedName + "\n" + TjStat.boosters((ReloadTurret) reloadTurretBuild.block, true, liquid))
                        : liquid -> new Option(liquid.uiIcon, liquid.localizedName)
                )).setFavorite(coolant.indexOf(coolant.max(liquid -> liquid.heatCapacity)));
                pack.get(2).setOptions(consumes.map(optionMapper));
            } else {
                turretBuild = null;
                pack.reset();
                if (control.input.config.getSelected() == self())
                    control.input.config.hideConfig();
            }
        }

        @Override
        public void updateTile() {
            heat = Mathf.lerpDelta(heat, pack.get(3).getIndex() > -1 ? 1f : 0f, 0.08f);
            if (turretBuild != null) {
                consumes.each(v -> {
                    if (v instanceof Item item)
                        turretBuild.handleStack(item, turretBuild.acceptStack(item, 1000000, this), this);
                    else if (v instanceof Liquid liquid)
                        turretBuild.liquids.set(liquid, Math.max(turretBuild.block.liquidCapacity, turretBuild.liquids.get(liquid)));
                });
                if (turretBuild instanceof ItemTurret.ItemTurretBuild build) {
                    if (getAmmo() instanceof Item item) {
                        if (build.ammo.size == 1 && ((ItemTurret.ItemEntry) build.ammo.first()).item == item) {
                            build.ammo.first().amount = build.totalAmmo = ((ItemTurret) build.block).maxAmmo;
                        } else {
                            build.ammo.clear();
                            build.handleItem(this, item);
                        }
                    } else {
                        build.ammo.clear();
                        build.totalAmmo = 0;
                        build.reloadCounter = 0f;
                    }
                } else {
                    if (getAmmo() instanceof Liquid liquid)
                        turretBuild.liquids.set(liquid, turretBuild.block.liquidCapacity);
                    else
                        ammoTypes.each(Liquid.class::isInstance, (Liquid liquid) -> turretBuild.liquids.set(liquid, 0f));
                }
                if (getCool() != null)
                    turretBuild.liquids.set(getCool(), turretBuild.block.liquidCapacity);
                else
                    coolant.each(liquid -> turretBuild.liquids.set(liquid, 0f));
                if (pack.get(3).getIndex() > -1)
                    turretBuild.applyBoost(overdrives[pack.get(3).getIndex()], 61.0f);
            }
        }

        protected void rebuild(Table table) {
            table.background(null).clear();
            buildConfiguration(table);
            table.pack();
        }

        @Override
        public void buildConfiguration(Table table) {
            if (turretBuild == null) return;
            if (!shown) {
                leftList(table, t -> t.button(Icon.eyeOffSmall, Styles.clearNonei, () -> {
                    shown = !shown;
                    rebuild(table);
                }).tooltip("@table.unshown", true).size(uiSize));
                table.table(Tex.pane, frame -> pack.build(frame));
                return;
            }
            table.background(Tex.pane).left();
            table.table(frame -> {
                frame.table(Styles.black6, list -> {
                    list.button(Icon.info, Styles.clearNonei, () -> {
                        ui.content.show(turretBuild.block);
                        Events.fire(new EventType.BlockInfoEvent());
                    }).tooltip("@info.title", true).size(uiSize).row();
                    list.button(Icon.wrench, Styles.clearNonei, () -> {
                        table.background(null).clear();
                        leftList(table, t -> t.button(Icon.undo, Styles.clearNonei, () -> rebuild(table)).tooltip("@back", true).size(uiSize));
                        table.table(Tex.pane, t -> layout.build(block, t, false));
                        table.pack();
                    }).tooltip(Core.bundle.format("table.change-turret", turretBuild.block.size), true).size(uiSize).row();
                    list.button(Icon.eyeSmall, Styles.clearNonei, () -> {
                        shown = !shown;
                        rebuild(table);
                    }).tooltip("@table.shown", true).size(uiSize).row();
                }).padRight(8f).top();
                frame.image(new TextureRegion(turretBuild.block.uiIcon)).tooltip(turretBuild.block.localizedName, true).size(100f).pad(10f);
                frame.image().color(Pal.gray).width(4f).pad(-9f, 8f, -9f, 8f).growY();
                pack.build(frame).row();
            }).row();
            table.image().color(Pal.gray).height(4f).pad(8f, -9f, 8f, -9f).growX().row();
            table.table(bars -> {
                bars.defaults().growX().minWidth(300f).height(18f).pad(4f);
                turretBuild.displayBars(bars);
            }).growX().row();
        }

        @Override
        public int[] config() {
            return pack.config();
        }

        @Override
        public float heat() {
            return turretBuild != null ? super.heat() : 0f;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            pack.write(write);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            pack.read(read);
        }
    }
}
