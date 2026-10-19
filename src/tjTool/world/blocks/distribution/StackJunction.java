package tjTool.world.blocks.distribution;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Interp;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Eachable;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.gen.Teamc;
import mindustry.graphics.Layer;
import mindustry.type.Item;
import mindustry.world.blocks.distribution.StackConveyor;
import mindustry.world.modules.ItemModule;

import static arc.math.geom.Geometry.d4;
import static mindustry.Vars.*;

public class StackJunction extends StackConveyor {
    public StackJunction(String name) {
        super(name);
        rotate = false;
        unloadable = false;
        hasItems = true;
        itemCapacity = 10;
        drawCached = false;
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        drawDefaultPlanRegion(plan, list);
    }

    @Override
    public boolean rotatedOutput(int x, int y) {
        return false;
    }

    public static class StackBuffer {
        public ItemModule items = new ItemModule();
        public StackConveyorBuild in;
        public StackConveyorBuild out;
        public float cooldown;

        public void reset() {
            in = null;
            out = null;
        }

        public void draw(Building building) {
            if (!items.any() || in == null) return;
            Tmp.v1.set(in).interpolate(Tmp.v2.set(building), 1 - cooldown, Interp.linear);
            var d = d4[in.relativeTo(building)];
            Tmp.v2.set(Tmp.v1).add(Tmp.v3.set(d.x * tilesize * (1 - cooldown), d.y * tilesize * (1 - cooldown)));
            float size = itemSize * Mathf.lerp(Math.min((float) items.total() / building.block.itemCapacity, 1), 1, 0.4f);
            Draw.z(Layer.blockAdditive + 0.01f);
            Draw.rect(((StackConveyor) in.block).stackRegion, Tmp.v1.x, Tmp.v1.y, in.rotdeg());
            Draw.rect(items.first().fullIcon, Tmp.v1.x, Tmp.v1.y, size, size);
            Draw.z(Layer.effect);
            Draw.color(items.first().color);
            Fill.circle(Tmp.v2.x, Tmp.v2.y, 2 * Mathf.sin(Mathf.pi * cooldown));
            Draw.color();
        }

        public void update(Building building, float speed, float recharge) {
            if (cooldown > 0) cooldown = Mathf.clamp(cooldown - speed, 0, recharge);
            if (in == null || out == null || cooldown > 0 || !building.enabled) return;
            var r = building.relativeTo(out);
            if (out.state != stateMove && r != out.rotation) out.state = stateMove;
            out.blendprox |= 1 << ((out.rotation - (r ^ 2) + 4) % 4);
            if (items.any()) {
                if (out.link != -1) return;
                out.items.add(items);
                out.lastItem = items.first();
                out.link = building.tile.pos();
                out.cooldown = 1;
                // ▲ to | from ▼
                items.clear();
            }
            if (in.state != stateLoad || (in.items.total() >= in.getMaximumAccepted(in.lastItem))) {
                if (in.link == -1 || in.cooldown > 0 || !in.enabled) return;
                items.add(in.items);
                cooldown = 1;
                in.link = -1;
                in.items.clear();
                in.cooldown = ((StackConveyor) in.block).recharge;
            }
        }

        public void write(Writes write) {
            items.write(write);
        }

        public void read(Reads read) {
            items.read(read);
        }
    }

    @SuppressWarnings("unused")
    public class StackJunctionBuild extends StackConveyorBuild {
        public StackBuffer[] buffers = new StackBuffer[]{new StackBuffer(), new StackBuffer()};

        // 暂时用不到 (我也不知道我当时为什么要写这个)
        public Building getItemDestination(Building source, Item item) {
            if (!this.enabled) return this;
            int dir = (source.relativeTo(this.tile.x, this.tile.y) + 4) % 4;
            Building next = this.nearby(dir);
            if (next != null)
                if (next instanceof StackJunctionBuild junction) return junction.getItemDestination(this, item);
                else if (next.acceptItem(this, item)) return next;
            return this;
        }

        @Override
        public void draw() {
            buffers[0].draw(this);
            buffers[1].draw(this);
            Draw.z(Layer.blockAdditive + 0.02f);
            Draw.rect(block.region, x, y);
        }

        @Override
        public void onProximityUpdate() {
            link = 0;
            state = stateMove;
            buffers[0].reset();
            buffers[1].reset();
            for (int i = 0; i < 4; i++) {
                int b = i % 2;
                if (nearby(i) instanceof StackConveyorBuild build && build.team == team) {
                    if (build instanceof StackJunctionBuild) continue;
                    if (build.relativeTo(this) != build.rotation)
                        buffers[b].out = buffers[b].out == null ? build : null;
                    else buffers[b].in = buffers[b].in == null ? build : null;
                }
            }
        }

        @Override
        public void updateTile() {
            var s = speed * (enabled ? (efficiency + baseEfficiency) : 1) * delta();
            buffers[0].update(this, s, recharge);
            buffers[1].update(this, s, recharge);
        }

        @Override
        public void dropped() {}

        @Override
        protected void poofIn() {}

        @Override
        protected void poofOut() {}

        @Override
        public boolean canUnload() {
            return false;
        }

        @Override
        public int acceptStack(Item item, int amount, Teamc source) {
            return 0;
        }

        @Override
        public void handleStack(Item item, int amount, Teamc source) {}

        @Override
        public boolean acceptItem(Building source, Item item) {
            return false;
        }

        @Override
        public void handleItem(Building source, Item item) {}

        @Override
        public void overwrote(Seq<Building> builds) {}

        @Override
        public void write(Writes write) {
            super.write(write);
            buffers[0].write(write);
            buffers[1].write(write);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            buffers[0].read(read);
            buffers[1].read(read);
        }
    }
}
