package tjTool.world.blocks.liquid;

import arc.Core;
import arc.func.Func;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.util.Nullable;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.gen.Building;
import mindustry.type.Liquid;
import mindustry.ui.Bar;
import mindustry.world.Tile;
import mindustry.world.blocks.liquid.Conduit;
import mindustry.world.blocks.liquid.LiquidBlock;
import mindustry.world.modules.LiquidModule;
import tjTool.world.blocks.TjBlock;

import static arc.math.geom.Geometry.d4;
import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;
import static tjTool.core.TjDraw.drawSelected;
import static tjTool.core.TjVars.halfSize;

// TODO Maybe we need MultiLiquidModule.
@SuppressWarnings("unused")
public class MultiConduit extends TjBlock {
    public MultiConduit(String name) {
        super(name);
        size = 2;
        hasLiquids = true;
        liquidCapacity = 40;
        outputsLiquid = true;
        rotate = true;
        canOverdrive = false;
        update = true;
        placeableLiquid = true;
        rotateDraw = true;
        displayFlow = false;
        // drawer = new DrawMulti(new DrawDefault());
    }

    @Override
    protected void loadDrawer() {
        super.loadDrawer();
    }

    public <T extends Building> void addLiquidBar(int i, Func<T, LiquidModule> liquid){
        addBar("liquid-" + i, (T build) -> new Bar(
                () -> liquid.get(build).current() != null && liquid.get(build).currentAmount() > 0.001f ? liquid.get(build).current().localizedName : Core.bundle.get("bar.liquid"),
                () -> liquid.get(build).current() != null ? liquid.get(build).current().barColor() : Color.clear,
                () -> liquid.get(build).current() != null ? liquid.get(build).currentAmount() / liquidCapacity : 0f)
        );
    }

    @Override
    public void setBars() {
        super.setBars();
        removeBar("liquid");
        addLiquidBar(0, (MultiConduitBuild b) -> b.liquidsLeft);
        addLiquidBar(1, (MultiConduitBuild b) -> b.liquidsMiddle);
        addLiquidBar(2, (MultiConduitBuild b) -> b.liquidsRight);
    }

    @Override
    public boolean rotatedOutput(int fromX, int fromY, Tile destination) {
        return destination.build instanceof Conduit.ConduitBuild && world.build(fromX, fromY) instanceof MultiConduitBuild self && self.getLiquidsTo(destination.build) != null;
    }

    @SuppressWarnings("unused")
    public class MultiConduitBuild extends TjBuilding {
        public LiquidModule liquidsLeft = new LiquidModule();
        public LiquidModule liquidsRight = new LiquidModule();
        public LiquidModule liquidsMiddle = new LiquidModule();
        public @Nullable MultiConduitBuild next;
        private boolean side = false;

        protected Building getBuilding(int rotationOffset, boolean front) {
            return getBuilding(front ? this.rotation : this.rotation ^ 2, rotationOffset);
        }

        protected Building getBuilding(int rotation, int rotationOffset) {
            var r = size / 2f * tilesize;
            return world.buildWorld(
                    x + (r + halfSize) * d4(rotation + rotationOffset).x + (r - halfSize) * d4(rotation).x,
                    y + (r + halfSize) * d4(rotation + rotationOffset).y + (r - halfSize) * d4(rotation).y);
        }

        public @Nullable LiquidModule getLiquidsTo(Building source) {
            LiquidModule liquids = null;
            var r = source.relativeTo(this);
            if (r == rotation) {
                for (int i = 0; i < 2; i += 1) {
                    var d = ((side ? i : i + 1) * 2 + 1) % 4;
                    if (getBuilding(rotation + d, d) == source) liquids = d != 3 ? liquidsLeft : liquidsRight;
                }
                return liquids;
            }
            return (r ^ rotation) != 2 && getBuilding(r - rotation, false) == source ? liquidsMiddle : null;
        }

        /**
         * 原本的{@code liquids}会作为复合液体模型的代理, 在尝试输入液体前请务必先调用此方法,
         * 无论是否希望强制输入流体, 否则直接输入会带来不确定的后果.
         * @param source Where the liquid from
         * @param liquid Input
         * @return Whether accept this liquid
         */
        @Override
        public boolean acceptLiquid(Building source, Liquid liquid) {
            if ((liquids = getLiquidsTo(source)) == null) return false;
            return liquids.current() == liquid || liquids.currentAmount() < 0.2f;
        }

        @Override
        @Nullable
        public Building getLiquidDestination(Building from, Liquid liquid) {
            side = !side;
            return acceptLiquid(from, liquid) ? this : null;
        }

        @Override
        public void drawSelect() {
            Draw.color(validColor(next != null));
            drawSelected(this);
            Draw.color(Color.acid);
            if (next != null) drawSelected(next);
        }

        @Override
        public void draw() {
            super.draw();
            LiquidBlock.drawTiledFrames(size, x, y,
                    rotation % 2 == 0 ? 0 : 4,
                    rotation % 2 == 0 ? 0 : 4,
                    rotation % 2 == 1 ? 0 : 4,
                    rotation % 2 == 1 ? 0 : 4,
                    liquidsMiddle.current(), liquidsMiddle.currentAmount() / liquidCapacity);
            LiquidBlock.drawTiledFrames(size,
                    x + d4(rotation + 1).x * 6,
                    y + d4(rotation + 1).y * 6,
                    rotation % 2 == 0 ? 0 : 6,
                    rotation % 2 == 0 ? 0 : 6,
                    rotation % 2 == 1 ? 0 : 6,
                    rotation % 2 == 1 ? 0 : 6,
                    liquidsLeft.current(), liquidsLeft.currentAmount() / liquidCapacity);
            LiquidBlock.drawTiledFrames(size,
                    x + d4(rotation - 1).x * 6,
                    y + d4(rotation - 1).y * 6,
                    rotation % 2 == 0 ? 0 : 6,
                    rotation % 2 == 0 ? 0 : 6,
                    rotation % 2 == 1 ? 0 : 6,
                    rotation % 2 == 1 ? 0 : 6,
                    liquidsRight.current(), liquidsRight.currentAmount() / liquidCapacity);
        }

        public void moveLiquid(LiquidModule out, LiquidModule in) {
            Liquid liquid = out.current();
            if (!(in.current() == liquid || in.currentAmount() < 0.2f)) return;
            float total = out.get(liquid) + in.get(liquid);
            float inLiquid = Mathf.clamp(total * 0.6f, Math.min(total, next.block.liquidCapacity * 0.1f), next.block.liquidCapacity);
            float outLiquid = total - inLiquid;
            out.add(liquid, outLiquid - out.get(liquid));
            in.add(liquid, inLiquid - in.get(liquid));
        }

        public void dumpSideLiquid(LiquidModule out, int rotationOffset) {
            if (out.currentAmount() == 0) return;
            var building = getBuilding(rotationOffset, true);
            Liquid liquid = out.current();
            if (building != null) building = building.getLiquidDestination(this, liquid);
            if (building != null && building.block.hasLiquids && building.acceptLiquid(this, liquid)) {
                float amount = Math.min(out.currentAmount(), building.block.liquidCapacity - building.liquids.get(liquid));
                building.handleLiquid(this, liquid, amount);
                out.add(liquid, -amount);
            }
        }

        public void moveLiquidForward() {
            if (next != null) {
                moveLiquid(liquidsMiddle, next.liquidsMiddle);
                moveLiquid(liquidsLeft, next.liquidsLeft);
                moveLiquid(liquidsRight, next.liquidsRight);
            } else dumpLiquid((liquids = liquidsMiddle).current(), 2, 0);
            dumpSideLiquid(liquidsLeft, 1);
            dumpSideLiquid(liquidsRight, -1);
        }

        @Override
        public void updateTile() {
            moveLiquidForward();
        }

        @Override
        public void onProximityUpdate() {
            next = nearby(size * d4[rotation].x, size * d4[rotation].y) instanceof MultiConduitBuild build &&
                    build.team == team && build.block.size == size &&
                    (build.tile.x - size * d4[rotation].x == tile.x && build.tile.y - size * d4[rotation].y == tile.y)
                    ? build : null;
        }

        @Override
        public void write(Writes write) {
            liquids = null;
            super.write(write);
            liquidsLeft.write(write);
            liquidsRight.write(write);
            liquidsMiddle.write(write);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            liquidsLeft.read(read);
            liquidsRight.read(read);
            liquidsMiddle.read(read);
        }
    }
}
