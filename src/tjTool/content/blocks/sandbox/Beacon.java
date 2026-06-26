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
import mindustry.world.blocks.storage.CoreBlock;
import tjTool.core.*;
import tjTool.core.TjTable.*;

import static mindustry.Vars.*;

public class Beacon extends SandboxBlock {
    public TextureRegion[] teamIcons = new TextureRegion[4];

    public Beacon(String name) {
        super(name);
        size = 5;
        configurable = true;
        placeEffect = new MultiEffect(placeEffect, TjEffect.place);

        config(Item.class, (BeaconBuild tile, Item v) -> {
            tile.c = v;
            tile.icon = v.uiIcon;
            tile.color = v.color;
        });
        config(Liquid.class, (BeaconBuild tile, Liquid v) -> {
            tile.c = v;
            tile.icon = v.uiIcon;
            tile.color = v.color;
        });
        config(Block.class, (BeaconBuild tile, Block v) -> {
            tile.c = v;
            tile.icon = v.uiIcon;
            tile.color = null;
        });
        config(UnitType.class, (BeaconBuild tile, UnitType v) -> {
            tile.c = v;
            tile.icon = v.uiIcon;
            tile.color = null;
        });
        configClear((BeaconBuild tile) -> {
            tile.c = null;
            tile.icon = null;
            tile.color = null;
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
        public Layout layout = new Layout(this::configure).with(
                new Page(Icon.box).with(Selection.unlockableContent(() -> content.items().as(), () -> c instanceof Item v ? v : null)),
                new Page(Icon.liquid).with(Selection.unlockableContent(() -> content.liquids().as(), () -> c instanceof Liquid v ? v : null)),
                new Page(Icon.crafting).with(Selection.unlockableContent(() -> content.blocks().select(this::canProduce).as(), () -> c instanceof Block v ? v : null)),
                new Page(Icon.units).with(Selection.unlockableContent(() -> content.units().select(this::canProduce).as(), () -> c instanceof UnitType v ? v : null))
        );

        public boolean canProduce(Block block) {
            return block.isVisible() && !(block instanceof CoreBlock) && !state.rules.isBanned(block) && block.environmentBuildable();
        }

        public boolean canProduce(UnitType unit) {
            return !unit.isHidden() && !unit.isBanned() && unit.supportsEnv(state.rules.env);
        }

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
            table.background(Tex.pane).left();
            layout.build(block, table, false);
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
