package tjTool.content.blocks.sandbox;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.content.Fx;
import mindustry.entities.effect.MultiEffect;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.world.blocks.defense.Wall;
import tjTool.core.*;

import static mindustry.Vars.tilesize;

public class Bedrock extends Wall {
    public Bedrock(String name) {
        super(name);
        size = 2;
        health = Integer.MAX_VALUE;
        update = true;
        placeEffect = new MultiEffect(placeEffect, Fx.drillSteam.startDelay(0f));
        breakEffect = new MultiEffect(breakEffect, Fx.mineImpact, TjEffect.blockBecomeSmaller(this));
        allowRectanglePlacement = true;
        placeableLiquid = true;
        alwaysUnlocked = true;
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

    @SuppressWarnings("unused")
    public class BedrockBuild extends Building {
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
            Draw.color(Color.HSVtoRGB((Time.time + x + y) % 360.0F, 25.0F, 100.0F, Tmp.c1.a(1f)));
            Draw.z(Layer.shields);
            Fill.rect(x, y, size * tilesize, size * tilesize);
            Draw.reset();
        }
    }
}
