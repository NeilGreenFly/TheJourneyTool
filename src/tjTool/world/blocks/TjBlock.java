package tjTool.world.blocks;

import arc.graphics.g2d.TextureRegion;
import arc.util.Eachable;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.draw.DrawBlock;
import mindustry.world.draw.DrawDefault;

public abstract class TjBlock extends Block {
    public DrawBlock drawer;

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

    protected void config() {}

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

    @SuppressWarnings("unused")
    public abstract class TjBuilding extends Building {
        @Override
        public void draw() {
            drawer.draw(this);
        }

        @Override
        public void drawLight() {
            super.drawLight();
            drawer.drawLight(this);
        }
    }
}
