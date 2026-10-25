package tjTool.world.blocks.sandbox;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.content.Fx;
import mindustry.entities.TargetPriority;
import mindustry.entities.effect.MultiEffect;
import mindustry.graphics.Layer;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.Env;
import tjTool.core.*;
import tjTool.world.blocks.TjBlock;

import static mindustry.Vars.tilesize;

public class Bedrock extends TjBlock {
    public Bedrock(String name) {
        super(name);
        size = 2;
        health = Integer.MAX_VALUE;
        update = true;
        canOverdrive = false;
        drawDisabled = false;
        placeEffect = new MultiEffect(placeEffect, Fx.drillSteam.startDelay(0f));
        breakEffect = new MultiEffect(breakEffect, Fx.mineImpact, TjEffect.blockBecomeSmaller(this));
        allowRectanglePlacement = true;
        placeableLiquid = true;
        alwaysUnlocked = true;
        group = BlockGroup.walls;
        priority = TargetPriority.wall;
        envEnabled = Env.any;
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(TjStat.config, TjStat.acknowledgements(region));
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("health", TjBar.makeHealthBalance);
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        TjDraw.drawPlace(this, x, y, valid);
    }

    public class BuildType extends TjBlock.BuildType {
        @Override
        public float handleDamage(float amount) {
            return 0f;
        }

        @Override
        public void updateTile() {
            maxHealth = block.health;
            heal();
        }

        @Override
        public void draw() {
            super.draw();
            Draw.z(Layer.shields);
            Draw.color(Color.HSVtoRGB((Time.time + x + y) % 360, 25, 100, Tmp.c1.a(1)));
            Fill.rect(x, y, size * tilesize, size * tilesize);
            Draw.reset();
        }
    }
}
