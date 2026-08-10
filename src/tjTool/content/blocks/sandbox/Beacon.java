package tjTool.content.blocks.sandbox;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.effect.MultiEffect;
import mindustry.game.Team;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.type.Item;
import mindustry.type.Liquid;
import mindustry.type.UnitType;
import mindustry.world.Block;
import tjTool.core.*;

import static mindustry.Vars.*;
import static tjTool.core.TjTable.*;

public class Beacon extends SandboxBlock {
    public static final TextureRegion[] teamIcons = new TextureRegion[4];

    public Beacon(String name) {
        super(name);
        size = 5;
        configurable = true;
        placeEffect = new MultiEffect(placeEffect, TjEffect.place);
    }

    @Override
    protected void config() {
        config(Item.class, (BeaconBuild build, Item v) -> {
            build.c = v;
            build.icon = v.uiIcon;
            build.color = v.color;
        });
        config(Liquid.class, (BeaconBuild build, Liquid v) -> {
            build.c = v;
            build.icon = v.uiIcon;
            build.color = v.color;
        });
        config(Block.class, (BeaconBuild build, Block v) -> {
            build.c = v;
            build.icon = v.uiIcon;
            build.color = null;
        });
        config(UnitType.class, (BeaconBuild build, UnitType v) -> {
            build.c = v;
            build.icon = v.uiIcon;
            build.color = null;
        });
        configClear((BeaconBuild build) -> {
            build.c = null;
            build.icon = null;
            build.color = null;
        });
    }

    @Override
    public void load() {
        super.load();
        for (int i = 0; i < 4; i += 1)
            teamIcons[i] = Core.atlas.find("team-" + Team.baseTeams[i]);
    }

    @SuppressWarnings("unused")
    public class BeaconBuild extends SandboxBuild {
        public UnlockableContent c;
        public TextureRegion icon;
        public Color color;
        public Layout layout = new Layout(this).with(
                new Page(Icon.box).with(Selection.unlockableContent(content.items()::as, () -> c instanceof Item v ? v : null)),
                new Page(Icon.liquid).with(Selection.unlockableContent(content.liquids()::as, () -> c instanceof Liquid v ? v : null)),
                new Page(Icon.crafting).with(Selection.unlockableContent(content.blocks().select(BaseSource::canProduce)::as, () -> c instanceof Block v ? v : null)),
                new Page(Icon.units).with(Selection.unlockableContent(content.units().select(BaseSource::canProduce)::as, () -> c instanceof UnitType v ? v : null))
        );

        public Color getColor() {
            return Tmp.c1.set(color == null ? team.color : color);
        }

        @Override
        public void draw() {
            super.draw();
            TjDraw.beacon(this, getColor(), icon != null ? icon : team.id < 4 ? teamIcons[team.id] : null);
            float f = (Time.time / 100f) % 1f;
            TjDraw.beacon(x, y, (size * tilesize / 2f) * f, getColor(), 1f - f);
        }

        @Override
        public void updateTile() {
            if (Mathf.chanceDelta(size * 0.02)) TjEffect.rising.at(x, y, size, getColor());
        }

        @Override
        public void buildConfiguration(Table table) {
            layout.build(block, table.background(Tex.pane), false);
        }

        @Override
        public Object config() {
            return c;
        }

        @Override
        public void read(Reads read, byte revision) {
            if (c != null) {
                if (c instanceof Item item) color = item.color;
                else if (c instanceof Liquid liquid) color = liquid.color;
            }
        }
    }
}
