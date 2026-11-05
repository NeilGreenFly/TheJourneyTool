package tjTool.world.blocks.sandbox;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.content.Fx;
import mindustry.entities.TargetPriority;
import mindustry.entities.effect.MultiEffect;
import mindustry.graphics.Layer;
import mindustry.world.meta.BlockGroup;
import tjTool.core.*;

import static mindustry.Vars.tilesize;

public class Bedrock extends SandboxBlock {
    public Bedrock(String name) {
        super(name);
        drawSelecting = false;
        size = 2;
        health = Integer.MAX_VALUE;
        update = true;
        drawDisabled = false;
        placeEffect = new MultiEffect(placeEffect, Fx.drillSteam.startDelay(0));
        breakEffect = new MultiEffect(breakEffect, Fx.mineImpact, TjEffect.blockBecomeSmaller(this));
        allowRectanglePlacement = true;
        group = BlockGroup.walls;
        priority = TargetPriority.wall;
    }

    public class BuildType extends SandboxBlock.BuildType {
        public float heat;

        @Override
        public float handleDamage(float amount) {
            heat = 1;
            return 0;
        }

        @Override
        public void updateTile() {
            heat = Mathf.lerpDelta(heat, 0, 0.02f);
            maxHealth = block.health;
            heal();
        }

        @Override
        public void draw() {
            super.draw();
            Draw.z(Layer.shields);
            Draw.color(Color.HSVtoRGB((Time.time + x + y) % 360, 25 * (1 - heat), 100, Tmp.c1.a(1)));
            Fill.rect(x, y, size * tilesize, size * tilesize);
            Draw.reset();
        }
    }
}
