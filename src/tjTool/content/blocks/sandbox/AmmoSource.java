package tjTool.content.blocks.sandbox;

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
import mindustry.gen.*;
import mindustry.graphics.Layer;
import mindustry.type.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.draw.*;
import tjTool.core.*;

import static mindustry.Vars.content;

public class AmmoSource extends BaseSource {
    public AmmoSource(String name) {
        super(name);
        rotate = true;
        regionRotated1 = 1;
        configurable = true;
        saveConfig = true;
        clearOnDoubleTap = true;
        drawer = new DrawMulti(
                new DrawDefault(),
                new DrawHeatOutput()
        );
        config(int[].class, (AmmoSourceBuild tile, int[] v) -> {
            switch (v[0]) {
                case -2:
                    tile.ammo = v[1];
                    tile.cool = v[2];
                    tile.overdrive = v[3];
                    break;
                case 0: tile.ammo = v[1]; break;
                case 1: tile.cool = v[1]; break;
                case 2: tile.overdrive = v[1]; break;
            }
        });
    }

    public class AmmoSourceBuild extends BaseSourceBuild {
        public @Nullable BaseTurret.BaseTurretBuild turretBuild = null;
        public int ammo = -1;
        public int cool = -1;
        public int overdrive = -1;
        public Seq<UnlockableContent> consumes = new Seq<>();
        public Seq<UnlockableContent> ammoTypes = new Seq<>();
        public Seq<Liquid> coolant = new Seq<>();
        public float[] overdrives = {1.5f, 2.25f, 2.5f};
        public float heat;

        public UnlockableContent getAmmo() {
            return (ammoTypes.any() && ammo > -1 && ammo < ammoTypes.size)
                    ? ammoTypes.get(ammo)
                    : null;
        }

        public Liquid getCool() {
            return (coolant.any() && cool > -1 && cool < coolant.size)
                    ? coolant.get(cool)
                    : null;
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
                drawItemSelection(getAmmo() != null ? getAmmo() : turretBuild.block);
                TjDraw.lightPoly(turretBuild, TjDraw.rainbow());
            }
        }

        @Override
        public void updateTile() {
            heat = Mathf.lerpDelta(heat, overdrive > -1 ? 1f : 0f, 0.08f);
            /*
            这边本来是初始化的，但是暂时不了解放在什么地方比较好，以及没有放置条件和不满足的回调，故目前为止暂时维持现状，以后我会来优化的。
             */
            consumes.clear();
            ammoTypes.clear();
            coolant.clear();
            Building b = front();
            if (checkBuild(b) && b instanceof BaseTurret.BaseTurretBuild baseTurretBuild && (turretBuild == baseTurretBuild || turretBuild == null)) {
                turretBuild = baseTurretBuild;
                if (getAmmo() instanceof Item item && !turretBuild.block.consumesItem(item)) ammo = -1;
                if (getAmmo() instanceof Liquid liquid && !turretBuild.block.consumesLiquid(liquid)) ammo = -1;
                if (getCool() != null && !turretBuild.block.consumesLiquid(getCool())) cool = -1;

                if (turretBuild.block.hasItems)
                    for (var item: content.items())
                        if (turretBuild.block.consumesItem(item))
                            consumes.add(item);
                if (turretBuild.block.hasLiquids)
                    for (var liquid: content.liquids())
                        if (turretBuild.block.consumesLiquid(liquid))
                            consumes.add(liquid);
                if (turretBuild instanceof ItemTurret.ItemTurretBuild) {
                    for (var v: consumes)
                        if (v instanceof Item)
                            ammoTypes.add(v);
                } else if (turretBuild instanceof LiquidTurret.LiquidTurretBuild) {
                    for (var v: consumes)
                        if (v instanceof Liquid)
                            ammoTypes.add(v);
                } else if (turretBuild instanceof ContinuousLiquidTurret.ContinuousLiquidTurretBuild) {
                    for (var v: consumes)
                        if (v instanceof Liquid)
                            ammoTypes.add(v);
                }
                for (var v: ammoTypes)
                    consumes.remove(v);
                if (((BaseTurret) turretBuild.block).coolant != null)
                    for (var liquid: content.liquids())
                        if (((BaseTurret) turretBuild.block).coolant.consumes(liquid)) {
                            coolant.add(liquid);
                            consumes.remove(liquid);
                        }
                for (var v: consumes)
                    if (v instanceof Item item && turretBuild.acceptItem(this, item))
                        turretBuild.handleItem(this, item);
                    else if (v instanceof Liquid liquid)
                        turretBuild.liquids.set(liquid, turretBuild.block.liquidCapacity);
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
                        for (var v: ammoTypes)
                            if (v instanceof Liquid liquid)
                                turretBuild.liquids.set(liquid, 0f);
                }
                if (getCool() != null)
                    turretBuild.liquids.set(getCool(), turretBuild.block.liquidCapacity);
                else
                    for (var liquid: coolant)
                        turretBuild.liquids.set(liquid, 0f);
                if (overdrive > -1)
                    turretBuild.applyBoost(overdrives[overdrive], 61.0f);
            } else {
                turretBuild = null;
                ammo = -1;
                cool = -1;
                overdrive = -1;
            }
//            if (turretBuild instanceof ReloadTurret.ReloadTurretBuild reloadTurretBuild && sf && (!ammoTypes.any() || getAmmo() != null))
//                reloadTurretBuild.reloadCounter = ((ReloadTurret) reloadTurretBuild.block).reload;
        }

        @Override
        public void buildConfiguration(Table table) {
            if (turretBuild != null) {
                table.clear();
                table.background(Tex.pane).top();
                TjConfigTable.rowTable(this, table, new Image(turretBuild.block.uiIcon), turretBuild.block.localizedName, ammoTypes, -1, () -> ammo, false, 0);
                if (coolant.any()) TjConfigTable.rowTable(this, table, new Image(Icon.star), "Boost", coolant, coolant.indexOf(coolant.max(liquid -> liquid.heatCapacity)), () -> cool, false, 1);
                if (consumes.any()) TjConfigTable.rowImageTable(table, new Image(Icon.download), "Consumes", consumes);
                if (turretBuild.block.canOverdrive) TjConfigTable.rowTable(this, table, new Image(Icon.effect), "Overdrive",
                        new Seq<>(new TextureRegion[]{Blocks.overdriveProjector.fullIcon, Blocks.overdriveProjector.fullIcon, Blocks.overdriveDome.fullIcon}),
                        new Seq<>(new String[]{"150%", "225%", "250%"}),
                        2, () -> overdrive, false, 2);
            }
        }

        @Override
        public int[] config() {
            return new int[]{-2, ammo, cool, overdrive};
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.i(ammo);
            write.i(cool);
            write.i(overdrive);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            ammo = read.i();
            cool = read.i();
            overdrive = read.i();
        }
    }
}
