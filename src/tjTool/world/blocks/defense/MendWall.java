package tjTool.world.blocks.defense;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.content.Fx;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.logic.Ranged;
import mindustry.type.Category;
import mindustry.world.blocks.defense.Wall;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

import static mindustry.Vars.*;

public class MendWall extends Wall {
    public Color baseColor = Color.valueOf("#8CAAEB");
    public float reload = 200f;
    public float range = 20f;
    public float healPercent = 0.2f;

    public MendWall(String name) {
        super(name);
        update = true;
        suppressable = true;
        emitLight = true;
        lightRadius = 50f;
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.repairTime, (int) (100f / healPercent * reload / 60f), StatUnit.seconds);
        stats.add(Stat.range, range / tilesize, StatUnit.blocks);
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, range, baseColor);
        indexer.eachBlock(player.team(), x * tilesize + offset, y * tilesize + offset, range, this::selectBy, this::selectDraw);
    }

    public boolean selectBy(Building other) {
        return other.block.category == Category.defense;
    }

    public void selectDraw(Building other) {
        Drawf.selected(other, Tmp.c1.set(baseColor).a(Mathf.absin(4f, 1f)));
    }

    @SuppressWarnings("unused")
    public class MendWallBuild extends Building implements Ranged {
        public float heat = 0f;
        public float charge = Mathf.random(reload);
        public float smoothEfficiency = 0f;
        public boolean anyTargets = false;

        @Override
        public float range() {
            return range;
        }

        @Override
        public void updateTile() {
            boolean canHeal = !checkSuppression();
            smoothEfficiency = Mathf.lerpDelta(smoothEfficiency, anyTargets ? efficiency : 0f, 0.08f);
            heat = Mathf.lerpDelta(heat, efficiency > 0 && canHeal ? 1f : 0f, 0.08f);
            charge += heat * delta();
            if (charge >= reload && canHeal) {
                charge = 0f;
                anyTargets = false;
                indexer.eachBlock(this, range,
                        b -> b.damaged() && !b.isHealSuppressed() && (b.block.category == Category.defense),
                        other -> {
                            other.heal(Math.max(other.maxHealth() * healPercent / 100f, 10f));
                            other.recentlyHealed();
                            Fx.healBlockFull.at(other.x, other.y, other.block.size, baseColor, other.block);
                            anyTargets = true;
                        });
            }
        }

        @Override
        public void drawSelect() {
            indexer.eachBlock(this, range, MendWall.this::selectBy, MendWall.this::selectDraw);
            Drawf.dashCircle(x, y, range, baseColor);
        }

        @Override
        public void draw() {
            super.draw();
            float f = 1f - (Time.time / 100f) % 1f;
            float r = size * tilesize / 2f;
            Draw.color(baseColor);
            Lines.stroke((2f * f + 0.2f) * smoothEfficiency);
            Lines.square(x, y, Math.min(1f + (1f - f) * r, r));
            Draw.reset();
        }

        @Override
        public void drawLight() {
            Drawf.light(x, y, lightRadius * smoothEfficiency, baseColor, 0.7f * smoothEfficiency);
        }
    }
}
