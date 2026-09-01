package tjTool.world.draw;

import arc.util.Eachable;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.draw.DrawBlock;

public abstract class DrawBase extends DrawBlock {
    public String suffix = "";
    public boolean drawPlan = true;
    public float offset = 0;

    public DrawBase() {}

    public DrawBase(String suffix) {
        this.suffix = suffix;
    }

    public DrawBase drawPlan(boolean drawPlan) {
        this.drawPlan = drawPlan;
        return this;
    }

    public DrawBase offset(float offset) {
        this.offset = offset;
        return this;
    }

    @Override
    public void drawPlan(Block block, BuildPlan plan, Eachable<BuildPlan> list) {
        if (drawPlan) draw(plan.drawx(), plan.drawy(), plan.rotation);
    }

    @Override
    public void draw(Building build) {
        draw(build.x, build.y, build.rotation);
    }

    public abstract void draw(float x, float y, int rotation);
}
