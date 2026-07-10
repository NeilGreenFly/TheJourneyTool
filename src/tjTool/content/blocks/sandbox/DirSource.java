package tjTool.content.blocks.sandbox;

import arc.Core;
import arc.Events;
import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.content.Blocks;
import mindustry.ctype.UnlockableContent;
import mindustry.game.EventType;
import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.type.Liquid;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Block;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.draw.*;
import tjTool.core.*;

import static mindustry.Vars.*;
import static tjTool.core.TjBundle.*;
import static tjTool.core.TjConfigTable.*;
import static tjTool.core.TjTable.*;

public class DirSource extends BaseSource {
    public static boolean shown = true;

    public DirSource(String name) {
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
        config(int[].class, (DirSourceBuild tile, int[] v) -> {
            if (tile.target != content.block(v[0])) return;
            tile.ammo = content.item(v[1]);
            tile.coolant = content.liquid(v[2]);
            tile.overdrive = v[3];
        });
        config(Block.class, (DirSourceBuild tile, Block v) -> {
            lastConfig = null;
            if (!(tile.targetBuild instanceof BaseTurret.BaseTurretBuild turret)) return;
            float rotation = turret.rotation;
            turret.tile.setBlock(v, tile.team);
            turret = (BaseTurret.BaseTurretBuild) tile.targetBuild;
            turret.rotation = rotation;
            turret.block.placeEffect.at(turret, turret.block.size);
        });
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(TjStat.config, table -> {
            table.row();
            TjStat.newConfigStats(table, Icon.turret.getRegion(), getBlock(name, "config-ammo"), getBlock(name, "config-ammo-description"));
            TjStat.newConfigStats(table, Icon.star.getRegion(), getBlock(name, "config-boost"), getBlock(name, "config-boost-description"));
            TjStat.newConfigStats(table, Icon.download.getRegion(), getBlock(name, "config-consumes"), getBlock(name, "config-consumes-description"));
            TjStat.newConfigStats(table, Icon.effect.getRegion(), getBlock(name, "config-overdrive"), getBlock(name, "config-overdrive-description"));
        });
    }

    @SuppressWarnings("unused")
    public class DirSourceBuild extends BaseSourceBuild {
        public @Nullable Building targetBuild = null;
        public @Nullable Block target = null;
        public @Nullable UnlockableContent ammo = null;
        public @Nullable Liquid coolant = null;
        public int overdrive = -1;
        public Seq<Item> items = new Seq<>();
        public Seq<Liquid> liquids = new Seq<>();
        public Seq<UnlockableContent> ammoTypes = new Seq<>();
        public Seq<Liquid> coolants = new Seq<>();
        public float[] overdrives = {1.5f, 2.25f, 2.5f};
        public float heat;

        public Pack pack = new Pack(this::configure).prefix(() -> w(target)).with(
                TypeContent.unlockableContent(() -> new Image(target.uiIcon), () -> target.localizedName, () -> ammoTypes, () -> ammo).setAlwaysBuild(true).setLock(() -> target instanceof BaseTurret),
                TypeContent.unlockableContent(() -> new Image(Icon.star), () -> getBlock(name, "config-boost"), coolants::as, v -> target instanceof ReloadTurret reloadTurret ? v.emoji() + v.localizedName + "\n" + TjStat.boosters(reloadTurret, true, (Liquid) v) : v.localizedName, () -> coolant).setFavorite(v -> ((Liquid) v).heatCapacity),
                TypeContent.unlockableContent(() -> new Image(Icon.download), () -> getBlock(name, "config-consumes"), () -> new Seq<UnlockableContent>(items).add(liquids), null),
                new IntContent(() -> new Image(Icon.effect), () -> getBlock(name, "config-overdrive"), () -> overdrive)
                        .add(new TextureRegionDrawable(Blocks.overdriveProjector.uiIcon), "150%")
                        .add(new TextureRegionDrawable(Blocks.overdriveProjector.uiIcon), "225%")
                        .add(new TextureRegionDrawable(Blocks.overdriveDome.uiIcon), "250%")
                        .setFavorite(2).setLock(() -> targetBuild != null && target.canOverdrive)
        );
        public Layout layout = new Layout(this::configure).with(
                new Page(Icon.wrench).with(Selection.unlockableContent(() -> content.blocks().select(block -> block instanceof BaseTurret && block.size == targetBuild.block.size).as(), () -> targetBuild != null ? targetBuild.block : null)),
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
            if (checkBuild(targetBuild))
                TjDraw.overdrive(targetBuild, "#ffd59e", heat);
        }

        @Override
        public void drawSelect() {
            super.drawSelect();
            if (targetBuild != null) {
                TjDraw.drawPlace(targetBuild.block, targetBuild.tile.x, targetBuild.tile.y, true);
                drawItemSelection(targetBuild.block);
                drawItemSelections(targetBuild, Seq.with(ammo, coolant));
            }
        }

        @Override
        public void drawConfigure() {
            super.drawConfigure();
            drawSelect();
        }

        @Override
        public void onProximityUpdate() {
            items.clear();
            liquids.clear();
            ammoTypes.clear();
            coolants.clear();
            if (!checkBuild(targetBuild = front())) {
                if (control.input.config.getSelected() == self())
                    control.input.config.hideConfig();
                target = null;
                ammo = null;
                coolant = null;
                overdrive = -1;
                return;
            }
            target = targetBuild.block;

            if (target.hasItems) content.items().each(target::consumesItem, items::add);
            if (target.hasLiquids) content.liquids().each(target::consumesLiquid, liquids::add);

            if (target instanceof BaseTurret baseTurret) {
                if (target instanceof ItemTurret) {
                    items.each(ammoTypes::add);
                    items.clear();
                }
                else if (target instanceof LiquidTurret || target instanceof ContinuousLiquidTurret) {
                    liquids.each(ammoTypes::add);
                    liquids.clear();
                }

                if (baseTurret.coolant != null) {
                    liquids.each(baseTurret.coolant::consumes, coolants::add);
                    coolants.each(liquids::remove);
                }
            }
        }

        @Override
        public void updateTile() {
            heat = Mathf.lerpDelta(heat, overdrive > -1 ? 1f : 0f, 0.08f);
            if (targetBuild == null) return;

            items.each(item -> targetBuild.handleStack(item, targetBuild.acceptStack(item, 1000000, this), this));
            liquids.each(liquid -> targetBuild.liquids.set(liquid, Math.max(targetBuild.block.liquidCapacity, targetBuild.liquids.get(liquid))));
            if (overdrive > -1) targetBuild.applyBoost(overdrives[overdrive], 61.0f);
            if (targetBuild.block.acceptsPayload)
                content.blocks().each(BaseSource::canProduce, v -> payloadPool(v, team, payload -> {
                    boolean b = targetBuild.acceptPayload(this, payload);
                    if (b) targetBuild.handlePayload(this, payload);
                    return b;
                }));
            if (targetBuild.block.acceptsUnitPayloads)
                content.units().each(BaseSource::canProduce, v -> payloadPool(v, team, payload -> {
                    boolean b = targetBuild.acceptPayload(this, payload);
                    if (b) targetBuild.handlePayload(this, payload);
                    return b;
                }));

            if (target instanceof BaseTurret) {
                if (targetBuild instanceof ItemTurret.ItemTurretBuild build) {
                    if (ammo instanceof Item item) {
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
                    if (ammo instanceof Liquid liquid)
                        targetBuild.liquids.set(liquid, targetBuild.block.liquidCapacity);
                    else
                        ammoTypes.each(Liquid.class::isInstance, (Liquid liquid) -> targetBuild.liquids.set(liquid, 0f));
                }
                if (coolant != null)
                    targetBuild.liquids.set(coolant, targetBuild.block.liquidCapacity);
                else
                    coolants.each(liquid -> targetBuild.liquids.set(liquid, 0f));
            }
        }

        protected void rebuild(Table table) {
            table.background(null).clear();
            buildConfiguration(table);
            table.pack();
        }

        @Override
        public void buildConfiguration(Table table) {
            if (targetBuild == null) return;
            if (!shown) {
                leftList(table, t -> t.button(Icon.eyeOffSmall, Styles.clearNonei, () -> {
                    shown = !shown;
                    rebuild(table);
                }).tooltip("@table.unshown", true).size(uiSize));
                table.table(Tex.pane, pack.build());
                return;
            }
            table.background(Tex.pane).left();
            table.table(frame -> {
                frame.table(Styles.black6, list -> {
                    list.button(Icon.info, Styles.clearNonei, () -> {
                        ui.content.show(targetBuild.block);
                        Events.fire(new EventType.BlockInfoEvent());
                    }).tooltip("@info.title", true).size(uiSize).row();
                    if (target instanceof BaseTurret) list.button(Icon.wrench, Styles.clearNonei, () -> {
                        table.background(null).clear();
                        leftList(table, t -> t.button(Icon.undo, Styles.clearNonei, () -> rebuild(table)).tooltip("@back", true).size(uiSize));
                        table.table(Tex.pane, t -> layout.build(block, t, false));
                        table.pack();
                    }).tooltip(Core.bundle.format("table.change-turret", targetBuild.block.size), true).size(uiSize).row();
                    list.button(Icon.eyeSmall, Styles.clearNonei, () -> {
                        shown = !shown;
                        rebuild(table);
                    }).tooltip("@table.shown", true).size(uiSize).row();
                }).padRight(8f).top();
                frame.image(new TextureRegion(targetBuild.block.uiIcon)).tooltip(targetBuild.block.localizedName, true).size(100f).pad(10f);
                frame.image().color(Pal.gray).width(4f).pad(-9f, 8f, -9f, 8f).growY();
                frame.table(pack.build()).row();
            }).left().row();
            table.image().color(Pal.gray).height(4f).pad(8f, -9f, 8f, -9f).growX().row();
            table.table(bars -> {
                bars.defaults().growX().minWidth(300f).height(18f).pad(4f);
                targetBuild.displayBars(bars);
            }).growX().row();
        }

        @Override
        public int[] config() {
            return pack.config();
        }

        @Override
        public float heat() {
            return targetBuild != null ? super.heat() : 0f;
        }

        protected <T extends UnlockableContent> short w(T t) {
            return t != null ? t.id : -1;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.b(ammo instanceof Item ? 0 : 1);
            write.s(w(ammo));
            write.s(w(coolant));
            write.b(overdrive);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            var b = read.b();
            var id = read.s();
            ammo = b == 0 ? content.item(id) : content.liquid(id);
            coolant = content.liquid(read.s());
            overdrive = read.b();
        }
    }
}
