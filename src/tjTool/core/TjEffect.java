package tjTool.core;

import arc.graphics.g2d.*;
import arc.math.geom.Vec2;
import mindustry.entities.Effect;
import mindustry.gen.Unit;
import mindustry.world.Block;
import mindustry.world.Tile;

import static arc.Core.camera;
import static arc.math.Angles.randLenVectors;
import static mindustry.Vars.*;

@SuppressWarnings("unused")
public class TjEffect {

    public static float z = 2f;

    public static Effect

    place = new Effect(120, e -> {
        Tile tile = world.tile((int) (e.x / tilesize), (int) (e.y / tilesize));
        if (tile.build != null) {
            Draw.color(tile.team().color.cpy());
            Draw.alpha(e.fout() * 2);
            Lines.stroke(e.fout() * 4);
            Lines.square(
                    e.x + (e.x - camera.position.x) * (z - 1) * e.fin(),
                    e.y + (e.y - camera.position.y) * (z - 1) * e.fin(),
                    e.rotation * tilesize * ((z - 1) * e.fin() + 1) / 2);
        }
    }),

    rising = new Effect(120, e -> randLenVectors(e.id, 1, e.finpow() * e.rotation * tilesize, (x, y) -> {
        Draw.color(e.color);
        Draw.alpha(e.fout() * 2);
        Fill.circle(
                e.x + x + (e.x - camera.position.x) * (z - 1) * e.fin(),
                e.y + y + (e.y - camera.position.y) * (z - 1) * e.fin(),
                4 * e.fin());
    })),

    dissociation = new Effect(120, e -> {
        if (!(e.data instanceof Vec2 v)) return;
        Draw.color(e.color);
        Fill.poly(e.x + v.x * e.fin() * 30, e.y + v.y * e.fin() * 30, 4, e.rotation * e.fout());
    }),

    callDissociation = new Effect(0, e -> {
        if (!(e.data instanceof Unit unit)) return;
        dissociation.at(e.x, e.y, unit.hitSize / 8, unit.team.color, unit.vel.cpy());
    }).followParent(false);

    public static Effect blockBecomeSmaller(Block block) {
        return new Effect(60, e -> randLenVectors(e.id, 1, e.finpow() * 5f, (x, y) -> {
            float r = block.size * tilesize * e.fout();
            Draw.rect(block.region, e.x + x, e.y + y, r, r);
        }));
    }

}
