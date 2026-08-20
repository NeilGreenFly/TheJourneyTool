package tjTool.world.blocks.distribution;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.util.Eachable;
import arc.util.Nullable;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.gen.Unit;
import mindustry.logic.LAccess;
import mindustry.type.Item;
import mindustry.ui.Styles;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.ItemSelection;
import mindustry.world.meta.BlockGroup;

import static mindustry.Vars.*;
import static tjTool.core.TjFunc.*;
import static tjTool.core.TjTable.*;

public class MultiSorter extends Block {
    public TextureRegion configRegion;
    public TextureRegion invertRegion;

    public MultiSorter(String name) {
        super(name);
        update = false;
        autoResetEnabled = false;
        destructible = true;
        underBullets = true;
        instantTransfer = true;
        unloadable = false;
        configurable = true;
        clearOnDoubleTap = true;
        saveConfig = true;
        drawDisabled = false;
        drawDynamic = true;
        drawCached = false;
        group = BlockGroup.transportation;

        config(short[].class, (TjSorterBuild build, short[] v) -> {
            if (build.enabled != (v[1] == 1)) placeEffect.at(build, size);
            build.sortItem = content.item(v[0]);
            build.enabled = v[1] == 1;
        });
        config(Item.class, (TjSorterBuild build, Item v) -> build.sortItem = v);
        config(Boolean.class, (TjSorterBuild build, Boolean v) -> build.enabled = v);
        configClear((TjSorterBuild build) -> build.sortItem = null);
    }

    @Override
    public void load() {
        super.load();
        configRegion = Core.atlas.find(name + "-item");
        invertRegion = Core.atlas.find(name + "-invert");
    }

    @Override
    protected TextureRegion[] icons() {
        return new TextureRegion[]{region};
    }

    @Override
    public void drawPlanConfig(BuildPlan plan, Eachable<BuildPlan> list) {
        if (!(plan.config instanceof short[] v)) return;
        if (v[1] == 0) Draw.rect(invertRegion, plan.drawx(), plan.drawy());
        if (v[0] < 0) return;
        Draw.color(content.item(v[0]).color, Draw.getColorAlpha());
        Draw.rect(configRegion, plan.drawx(), plan.drawy());
        Draw.color();
    }

    @Override
    public int minimapColor(Tile tile) {
        var build = (TjSorterBuild) tile.build;
        return build != null && build.sortItem != null ? build.sortItem.color.rgba() : 0;
    }

    @Override
    public boolean outputsItems() {
        return true;
    }

    @SuppressWarnings("unused")
    public class TjSorterBuild extends Building {
        public @Nullable Item sortItem;

        @Override
        public void configured(Unit player, Object value) {
            super.configured(player, value);
            if (!headless) {
                recache();
                renderer.minimap.update(tile);
            }
        }

        @Override
        public void draw() {
            Draw.rect(enabled ? region : invertRegion, x, y, drawrot());
            if (sortItem != null) {
                Draw.color(sortItem.color);
                Draw.rect(configRegion, x, y);
                Draw.color();
            }
        }

        @Override
        public void drawSelect() {
            drawItemSelection(sortItem);
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            Building to = getTileTarget(item, source, false);
            return to != null && to.acceptItem(this, item) && to.team == team;
        }

        @Override
        public void handleItem(Building source, Item item) {
            getTileTarget(item, source, true).handleItem(this, item);
        }

        protected boolean isSame(Building other) {
            return other != null && other.block.instantTransfer;
        }

        protected Building getTileTarget(Item item, Building source, boolean flip) {
            int dir = source.relativeTo(tile.x, tile.y);
            if (dir == -1) return null;
            if ((item == sortItem) == enabled) { // prevent 3-chains
                if (isSame(source) && isSame(nearby(dir))) return null;
                return nearby(dir);
            } else {
                Building a = nearby(Mathf.mod(dir - 1, 4));
                Building b = nearby(Mathf.mod(dir + 1, 4));
                boolean ac = a != null && !(a.block.instantTransfer && source.block.instantTransfer) && a.acceptItem(this, item);
                boolean bc = b != null && !(b.block.instantTransfer && source.block.instantTransfer) && b.acceptItem(this, item);
                if (ac && !bc) return a;
                else if (bc && !ac) return b;
                else if (!bc) return null;
                else {
                    Building to = (rotation & (1 << dir)) == 0 ? a : b;
                    if (flip) rotation ^= (1 << dir);
                    return to;
                }
            }
        }

        protected short[] configPack(Item item, boolean enabled) {
            return new short[]{item != null ? item.id : -1, (short) byBool(enabled)};
        }

        @Override
        public void buildConfiguration(Table table) {
            leftList(table, t -> t.button(Icon.wrench, Styles.clearNonei, () -> configure(configPack(sortItem, !enabled))).tooltip("@table.toggle", true).size(uiSize));
            ItemSelection.buildTable(block, table, content.items(), () -> sortItem, item -> configure(configPack(item, enabled)), selectionRows, selectionColumns);
        }

        @Override
        public Object config() {
            return configPack(sortItem, enabled);
        }

        @Override
        public Object senseObject(LAccess sensor) {
            if (sensor == LAccess.config) return sortItem;
            return super.senseObject(sensor);
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.s(sortItem != null ? sortItem.id : -1);
            write.bool(enabled);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            sortItem = content.item(read.s());
            enabled = read.bool();
        }
    }
}
