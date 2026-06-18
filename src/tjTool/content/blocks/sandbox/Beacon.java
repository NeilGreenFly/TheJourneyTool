package tjTool.content.blocks.sandbox;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.util.Time;
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
import tjTool.core.TjDraw;
import tjTool.core.TjEffect;
import tjTool.core.TjTable.*;

import static mindustry.Vars.*;
import static mindustry.Vars.state;

public class Beacon extends SandboxBlock {
    public TextureRegion[] teamIcons = new TextureRegion[4];

    public Beacon(String name) {
        super(name);
        size = 5;
        configurable = true;
        placeEffect = new MultiEffect(placeEffect, TjEffect.place);
        config(int[].class, (BeaconBuild tile, int[] v) -> {
            for (var i : v)
                if (i != -1 && i != tile.index) {
                    tile.index = i;
                    return;
                }
            tile.index = -1;
        });
    }

    @Override
    public void load() {
        super.load();
        for (int i = 0; i < 4; i += 1)
            teamIcons[i] = Core.atlas.find("team-" + Team.baseTeams[i]);
    }

    public static int flag = 65536;

    @SuppressWarnings("unused")
    public class BeaconBuild extends SandboxBuild {
        public Color color = team.color;
        // public TextureRegion region = team.id < 4 ? teamIcons[team.id] : null;
        public int index = -1;
        public Layout layout = new Layout(this::configure).with(
                new Selection<>(content.items(), v -> v.uiIcon, v -> v.localizedName, () -> content.item(unPack(index, 0)), v -> pack(v.id, 0)).setIcon(Icon.box),
                new Selection<>(content.liquids(), v -> v.uiIcon, v -> v.localizedName, () -> content.liquid(unPack(index, 1)), v -> pack(v.id, 1)).setIcon(Icon.liquid),
                new Selection<>(content.blocks().select(this::canProduce), v -> v.uiIcon, v -> v.localizedName, () -> content.block(unPack(index, 2)), v -> pack(v.id, 2)).setIcon(Icon.crafting),
                new Selection<>(content.units().select(this::canProduce), v -> v.uiIcon, v -> v.localizedName, () -> content.unit(unPack(index, 3)), v -> pack(v.id, 3)).setIcon(Icon.units)
        );

        public boolean canProduce(Block block){
            return block.isVisible() && !(block instanceof CoreBlock) && !state.rules.isBanned(block) && block.environmentBuildable();
        }

        public boolean canProduce(UnitType unit){
            return !unit.isHidden() && !unit.isBanned() && unit.supportsEnv(state.rules.env);
        }

        public int pack(short id, int cat) {
            return (int) id + flag * cat;
        }
        public int unPack(int index, int cat) {
            if (index == -1 || index >> 16 != cat) return -1;
            return index & (flag - 1);
        }

        @Override
        public void draw() {
            super.draw();
            UnlockableContent v = null;
            color = team.color;
            if (index != -1) {
                int cat = index >> 16;
                int id = unPack(index, cat);
                v = switch (cat) {
                    case 0 -> content.item(id);
                    case 1 -> content.liquid(id);
                    case 2 -> content.block(id);
                    case 3 -> content.unit(id);
                    default -> null;
                };
                if (v instanceof Item item) color = item.color;
                else if (v instanceof Liquid liquid) color = liquid.color;
            }
            TjDraw.beacon(this, color.cpy(), v != null ? v.uiIcon : (team.id < 4 ? teamIcons[team.id] : null));
            float f = (Time.time / 100f) % 1f;
            TjDraw.beacon(x, y, (size * tilesize / 2f) * f, color.cpy(), 1f - f);
        }

        @Override
        public void buildConfiguration(Table table) {
            table.background(Tex.pane).left();
            layout.build(block, table, false);
        }

        @Override
        public void updateTile() {
            if (Mathf.chanceDelta(size * 0.02)) TjEffect.rising.at(x, y, size, color);
        }
    }
}
