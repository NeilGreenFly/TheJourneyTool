package tjTool.world.blocks;

import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.util.Eachable;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.graphics.Pal;
import mindustry.world.Block;
import mindustry.world.draw.DrawBlock;
import mindustry.world.draw.DrawDefault;

public abstract class TjBlock extends Block {
    public DrawBlock drawer = new DrawDefault();

    public static Color validColor(boolean valid) {
        return valid ? Pal.accent : Pal.remove;
    }

    public TjBlock(String name) {
        super(name);
        config();
        destructible = true;
        rotateDraw = false;
        noUpdateDisabled = true;
        selectionRows = 5;
        selectionColumns = 6;
    }

    protected void config() {}

    protected void loadDrawer() {
        drawer.load(this);
    }

    @Override
    public void load() {
        super.load();
        loadDrawer();
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) {
        drawer.drawPlan(this, plan, list);
    }

    @Override
    protected TextureRegion[] icons() {
        return drawer.finalIcons(this);
    }

    public abstract class TjBuilding extends Building {
        public static <T extends UnlockableContent> short w(T t) {
            return t != null ? t.id : -1;
        }

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
