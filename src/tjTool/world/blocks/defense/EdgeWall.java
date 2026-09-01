package tjTool.world.blocks.defense;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.world.Tile;
import mindustry.world.blocks.defense.Wall;
import tjTool.world.AutoTile;

import static arc.Core.*;
import static arc.math.geom.Geometry.d8;
import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;
import static tjTool.world.AutoTile.*;

public class EdgeWall extends Wall {
    public TextureRegion[] regions;
    public TextureRegion regionLarge;
    public Color baseColor = Color.valueOf("#C8C8E4");

    public EdgeWall(String name) {
        super(name);
        allowRectanglePlacement = true;
    }

    @Override
    public void load() {
        super.load();
        region = atlas.find(name + "-atlas");
        regions = atlasRegions(region);
        regionLarge = atlasLarge(region);
        uiIcon = atlasUI(region);
        fullIcon = regions[0];
        drawDynamic = true;
        drawCached = false;
    }

    @Override
    public TextureRegion getDisplayIcon(Tile tile) {
        return super.getDisplayIcon(tile);
    }

    @Override
    public TextureRegion[] getGeneratedIcons() {
        return generatedIcons == null ? (generatedIcons = new TextureRegion[]{fullIcon}) : generatedIcons;
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        Drawf.dashSquare(baseColor, (x & ~1) * tilesize + 4, (y & ~1) * tilesize + 4, 2 * tilesize);
    }

    @SuppressWarnings("unused")
    public class EdgeWallBuild extends WallBuild implements AutoTile {
        protected int index = 0;
        protected boolean isLarge = false;
        protected boolean alignedLarge = false;

        @Override
        public boolean checkBuild(Building other) {
            return other instanceof EdgeWallBuild && other.block == block && other.team == team;
        }

        @Override
        public void draw() {
            if (!isLarge) Draw.rect(regions[(tile.x + tile.y) % 2 + 33], x, y);
            else if (alignedLarge) Draw.rect(regionLarge, x + 4, y + 4);
            if (index != 255) {
                Draw.z(Layer.block + 0.1f);
                Draw.rect(regions[tileMap[index]], x, y);
            }
        }

        @Override
        public void drawTeam() {
            if (!isLarge || alignedLarge) {
                Draw.z(Layer.block + 0.2f);
                super.drawTeam();
            }
        }

        @Override
        public void updateProximity() {
            super.updateProximity();
            proximityTileUpdate();
            for (var point : d8)
                if (world.build(tile.x + point.x, tile.y + point.y) instanceof EdgeWallBuild other && other.team == team)
                    other.proximityTileUpdate();
        }

        public void proximityTileUpdate() {
            int xLarge = tile.x & ~1;
            int yLarge = tile.y & ~1;
            index = getIndex8(this);
            isLarge =
                    checkBuild(world.build(xLarge, yLarge)) &&
                    checkBuild(world.build(xLarge + 1, yLarge)) &&
                    checkBuild(world.build(xLarge, yLarge + 1)) &&
                    checkBuild(world.build(xLarge + 1, yLarge + 1));
            alignedLarge = tile.x == (tile.x & ~1) && tile.y == (tile.y & ~1);
        }
    }
}
