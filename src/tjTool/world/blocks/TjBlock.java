package tjTool.world.blocks;

import arc.func.Cons2;
import arc.graphics.g2d.TextureRegion;
import arc.scene.ui.layout.Table;
import arc.util.Eachable;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.draw.DrawBlock;
import mindustry.world.draw.DrawDefault;

import static mindustry.Vars.tilesize;

public abstract class TjBlock extends Block {
    public DrawBlock drawer;

    /** if true, double-tapping this configurable block to deselect it. */
    public boolean deselectOnDoubleTap;

    public TjBlock(String name) {
        super(name);
        config();
        solid = true;
        destructible = true;
        rotateDraw = false;
        noUpdateDisabled = true;
        selectionRows = 5;
        selectionColumns = 6;
    }

    /**
     * @see mindustry.io.TypeIO
     */
    protected void config() {}

    protected <T extends BuildType> void config(int count, Cons2<T, String[]> config) {
        config(String.class, (T building, String args) -> {
            var arr = args.split(";");
            if (arr.length == count) config.get(building, arr);
        });
    }

    protected DrawBlock defaultDrawer() {
        return new DrawDefault();
    }

    @Override
    public void load() {
        super.load();
        if (drawer == null) drawer = defaultDrawer();
        drawer.load(this);
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) {
        drawer.drawPlan(this, plan, list);
    }

    @Override
    protected TextureRegion[] icons() {
        return drawer.finalIcons(this);
    }

    public void drawPlaceText(String text, Tile tile, boolean valid) { // @return float
        drawPlaceText(text, tile.x, tile.y, valid);
    }

    public float pos(int n) {
        return n * tilesize + offset;
    }

    public abstract class BuildType extends Building {
        @Override
        public void draw() {
            drawer.draw(this);
        }

        @Override
        public void drawLight() {
            super.drawLight();
            drawer.drawLight(this);
        }

        protected void rebuild(Table table) {
            table.background(null).clear();
            buildConfiguration(table);
            table.pack();
        }

        @Override
        public boolean onConfigureBuildTapped(Building other) {
            if (!deselectOnDoubleTap) return super.onConfigureBuildTapped(other);
            if (this == other) deselect();
            return this != other;
        }
    }
}
