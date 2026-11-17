package tjTool.world.blocks.distribution;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.util.Eachable;
import arc.util.Nullable;
import arc.util.Strings;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.gen.Unit;
import mindustry.logic.LAccess;
import mindustry.type.Item;
import mindustry.ui.Styles;
import mindustry.world.Tile;
import mindustry.world.blocks.ItemSelection;
import mindustry.world.meta.BlockGroup;
import tjTool.content.TjSettings;
import tjTool.world.blocks.TjBlock;

import static mindustry.Vars.*;
import static tjTool.widgets.TjTable.*;
import static tjTool.world.LazyGetter.*;

public class MultiSorter extends TjBlock {
    public TextureRegion configRegion;
    public TextureRegion invertRegion;

    public MultiSorter(String name) {
        super(name);
        solid = false;
        update = false;
        noUpdateDisabled = false;
        autoResetEnabled = false;
        underBullets = true;
        instantTransfer = true;
        unloadable = false;
        canOverdrive = false;
        configurable = true;
        saveConfig = true;
        clearOnDoubleTap = true;
        drawDisabled = false;
        drawDynamic = true;
        drawCached = false;
        group = BlockGroup.transportation;
    }

    @Override
    protected void config() {
        config(Item.class, (BuildType build, Item v) -> build.sortItem = v);
        config(Boolean.class, (BuildType build, Boolean v) -> build.enabled = v);
        configClear((BuildType build) -> build.sortItem = null);
        config(2, (BuildType build, String[] arr) -> {
            boolean b = build.enabled;
            build.sortItem = content.item(Strings.parseInt(arr[0]));
            build.enabled = parseBool(arr[1]);
            if (build.enabled != b) placeEffect.at(build, size);
        });
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
        if (!(plan.config instanceof String args)) return;
        String[] arr = args.split(";");
        if (arr.length != 2) return;
        var item = content.item(Strings.parseInt(arr[0]));
        if (!parseBool(arr[1])) Draw.rect(invertRegion, plan.drawx(), plan.drawy());
        if (item == null) return;
        Draw.color(item.color, Draw.getColorAlpha());
        Draw.rect(configRegion, plan.drawx(), plan.drawy());
        Draw.color();
    }

    @Override
    public int minimapColor(Tile tile) {
        return c(((BuildType) tile.build).sortItem).rgba();
    }

    @Override
    public boolean outputsItems() {
        return true;
    }

    public class BuildType extends TjBlock.BuildType {
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
            Draw.rect(enabled ? region : invertRegion, x, y);
            if (sortItem != null) {
                Draw.color(sortItem.color);
                Draw.rect(configRegion, x, y);
                Draw.color();
                if (TjSettings.arcChoiceUiIcon.getBool()) {
                    Draw.scl(0.5f);
                    Draw.rect(sortItem.uiIcon, x, y);
                    Draw.reset();
                }
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

        @Nullable
        protected Building getTileTarget(Item item, Building source, boolean flip) {
            int dir = source.relativeTo(tile.x, tile.y);
            if (dir == -1) return null;
            if ((item == sortItem) == enabled) return isSame(source) && isSame(nearby(dir)) ? null : nearby(dir); // prevent 3-chains
            Building a = nearby(Mathf.mod(dir - 1, 4));
            Building b = nearby(Mathf.mod(dir + 1, 4));
            boolean ac = a != null && !(a.block.instantTransfer && source.block.instantTransfer) && a.acceptItem(this, item);
            boolean bc = b != null && !(b.block.instantTransfer && source.block.instantTransfer) && b.acceptItem(this, item);
            if (ac && !bc) return a;
            else if (bc && !ac) return b;
            else if (!bc) return null;
            Building to = (rotation & (1 << dir)) == 0 ? a : b;
            if (flip) rotation ^= (1 << dir);
            return to;
        }

        protected String serialize(Item item, boolean enabled) {
            return String.join(";", String.valueOf(w(item)), String.valueOf(Mathf.num(enabled)));
        }

        @Override
        public void buildConfiguration(Table table) {
            leftList(table, t -> t.button(Icon.wrench, Styles.clearNonei, () -> configure(serialize(sortItem, !enabled))).tooltip("@table.toggle", true).size(uiSize));
            ItemSelection.buildTable(block, table, content.items(), () -> sortItem, item -> configure(serialize(item, enabled)), selectionRows, selectionColumns);
        }

        @Override
        public Object config() {
            return serialize(sortItem, enabled);
        }

        @Override
        public Object senseObject(LAccess sensor) {
            if (sensor == LAccess.config) return sortItem;
            return super.senseObject(sensor);
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.s(w(sortItem));
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
