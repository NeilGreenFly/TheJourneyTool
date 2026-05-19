package tjTool.content.blocks.sandbox;

import arc.scene.ui.layout.Table;
import arc.util.Align;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.world.blocks.defense.BaseShield;
import tjTool.core.TjBar;
import tjTool.core.TjDraw;
import tjTool.core.TjStat;

import static mindustry.Vars.tilesize;

public class ShieldSource extends BaseShield {
    public ShieldSource(String name) {
        super(name);
        size = 3;
        hasPower = false;
        rebuildable = true;
        configurable = true;
        saveConfig = true;
        alwaysUnlocked = true;
        placeableLiquid = true;
        config(Float.class, (ShieldSourceBuild tile, Float v) -> tile.optionalEfficiency = v);
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
        Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, (lastConfig != null ? (float) lastConfig : 0) * radius, TjDraw.rainbow());
    }

    @SuppressWarnings("unused")
    public class ShieldSourceBuild extends BaseShieldBuild {
        @Override
        public void buildConfiguration(Table table) {
            float iconSize = 32f;
            table.background(Tex.pane).left();
            table.image(region).tooltip(localizedName, true).size(iconSize).pad(10);
            table.slider(0, 1, 0.05f, optionalEfficiency, this::configure).width(200).row();
            table.image(Icon.resize).tooltip("Range", true).size(iconSize).pad(10);
            table.label(() -> String.format("%.2f / %.2f", optionalEfficiency * radius / 8, radius / 8)).color(enabled ? Pal.accent : Pal.remove).growX().labelAlign(Align.right);
        }

        @Override
        public void updateConsumption() {
            efficiency = enabled ? optionalEfficiency : 0;
        }

        @Override
        public void drawSelect() {
            super.drawSelect();
            Drawf.dashCircle(x, y, optionalEfficiency * radius, TjDraw.rainbow());
        }

        @Override
        public void drawConfigure() {
            drawSelect();
            TjDraw.lightPoly(this, TjDraw.rainbow());
        }

        @Override
        public Object config() {
            return optionalEfficiency;
        }
    }
}
