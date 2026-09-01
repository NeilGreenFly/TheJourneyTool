package tjTool.world.blocks.liquid;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.util.Nullable;
import mindustry.gen.Building;
import mindustry.type.Liquid;
import mindustry.world.blocks.liquid.LiquidBlock;
import mindustry.world.draw.DrawDefault;
import mindustry.world.draw.DrawLiquidTile;
import mindustry.world.draw.DrawMulti;
import mindustry.world.modules.LiquidModule;
import tjTool.world.blocks.TjBlock;

import static arc.math.geom.Geometry.d4;
import static arc.math.geom.Geometry.d8edge;
import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;
import static tjTool.core.TjDraw.drawSelected;
import static tjTool.core.TjVars.halfSize;

/**
 * {@code Anuke}的液体系统真是神神又奇奇，我拼尽全力无法战胜...
 */
@SuppressWarnings("unused")
public class MultiConduit extends TjBlock {
    public MultiConduit(String name) {
        super(name);
        size = 2;
        hasLiquids = true;
        liquidCapacity = 40;
        rotate = true;
        canOverdrive = false;
        update = true;
        placeableLiquid = true;
        rotateDraw = true;
        drawer = new DrawMulti(new DrawLiquidTile(), new DrawDefault());
        // requirements(liquid, with(beryllium, 16));
    }

    @SuppressWarnings("unused")
    public class MultiConduitBuild extends TjBuilding {
        public LiquidModule liquidsLeft = new LiquidModule();
        public LiquidModule liquidsRight = new LiquidModule();
        public @Nullable MultiConduitBuild next;

        public @Nullable LiquidModule getLiquidsTo(Building source) {
            return source.relativeTo(this) == rotation ? ((rotation % 2 != 0
                    ? source.x - x
                    : source.y - y
            ) * d8edge[rotation].x > 0 ? liquidsLeft : liquidsRight) : ((rotation % 2 == 0
                    ? source.x - x
                    : source.y - y
            ) * d8edge[rotation].y < 0 ? liquids : null);
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid) {
            var it = getLiquidsTo(source);
            return it != null && (it.current() == liquid || it.currentAmount() < 0.2f) && it.currentAmount() < liquidCapacity;
        }

        @Override
        public void handleLiquid(Building source, Liquid liquid, float amount) {
            var it = getLiquidsTo(source);
            if (it != null) it.add(liquid, amount);
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
            float inLiquid = Math.min(next.block.liquidCapacity, total * 0.6f);
            float outLiquid = total - inLiquid;
            out.add(liquid, outLiquid - out.get(liquid));
            in.add(liquid, inLiquid - in.get(liquid));
        }

        public void dumpSideLiquid(LiquidModule out, int rotationOffset) {
            if (out.currentAmount() == 0) return;
            var building = world.buildWorld(
                    x + (size / 2f * tilesize + halfSize) * d4(rotation + rotationOffset).x + (size / 2f * tilesize - halfSize) * d4[rotation].x,
                    y + (size / 2f * tilesize + halfSize) * d4(rotation + rotationOffset).y + (size / 2f * tilesize - halfSize) * d4[rotation].y);
            if (building == null) return;
            Liquid liquid = out.current();
            building = building.getLiquidDestination(building, liquid);
            if (building.block.hasLiquids) {
                float amount = Math.min(out.currentAmount(), building.block.liquidCapacity - building.liquids.get(liquid));
                building.handleLiquid(this, liquid, amount);
                out.add(liquid, -amount);
            }
        }

        public void moveLiquidForward() {
            if (next != null) {
                moveLiquid(liquids, next.liquids);
                moveLiquid(liquidsLeft, next.liquidsLeft);
                moveLiquid(liquidsRight, next.liquidsRight);
            } else dumpLiquid(liquids.current(), 2, rotation);
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
    }
}
