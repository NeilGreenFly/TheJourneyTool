package tjTool.world.blocks.power;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.util.Tmp;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.gen.Building;
import mindustry.world.blocks.power.PowerBlock;
import mindustry.world.meta.Env;
import tjTool.world.AutoTile;

import static arc.graphics.g2d.Draw.color;
import static arc.graphics.g2d.Lines.stroke;
import static mindustry.Vars.*;
import static mindustry.world.blocks.power.PowerNode.makeBatteryBalance;
import static mindustry.world.blocks.power.PowerNode.makePowerBalance;
import static tjTool.core.TjFunc.forRange;

/** Stay tuned */
@SuppressWarnings("unused")
public class PowerWire extends PowerBlock {
    public TextureRegion[] regions;
    protected static byte[] status = new byte[]{0, 0, 0, 2, 0, 1, 2, 3, 0, 2, 1, 3, 2, 3, 3, 4};
    protected static byte[] rotations = new byte[]{0, 0, 1, 0, 2, 0, 1, 2, 3, 3, 1, 1, 2, 0, 3, 0};

    public PowerWire(String name) {
        super(name);
        size = 1;
        destructible = true;
        outputsPower = false;
        consumesPower = false;
        canOverdrive = false;
        solid = false;
        underBullets = true;
        envEnabled |= Env.space;

        placeEffect = new Effect(16, e -> {
            color(Color.valueOf(Tmp.c1.a(1), "#8CAAEB"));
            stroke(3f - e.fin() * 2f);
            Lines.square(e.x, e.y, tilesize / 2f * e.rotation + e.fin() * 3f);
        });
    }

    @Override
    public void setBars(){
        super.setBars();
        addBar("power", makePowerBalance());
        addBar("batteries", makeBatteryBalance());
    }

    @Override
    public void load() {
        super.load();
        regions = new TextureRegion[5];
        forRange(regions.length, i -> regions[i] = Core.atlas.find(name + "-" + i));
    }

    @Override
    protected TextureRegion[] icons(){
        return new TextureRegion[]{regions[0]};
    }

    @SuppressWarnings("unused")
    public class PowerWireBuild extends Building implements AutoTile {
        public int bits;

        @Override
        public boolean checkBuild(Building other) {
            return other instanceof PowerWireBuild && other.block == block && other.team == team;
        }

        @Override
        public void draw() {
            Draw.rect(regions[status[bits]], x, y, rotations[bits] * 90);
        }

        @Override
        public void onProximityUpdate() {
            bits = getIndex4(this);
            Fx.healBlockFull.at(x, y, size);
        }
    }
}
