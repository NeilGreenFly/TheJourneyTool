package tjTool.content;

import arc.graphics.Color;
import mindustry.content.*;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.Category;
import mindustry.world.Block;
import mindustry.world.blocks.defense.*;
import mindustry.world.blocks.liquid.LiquidBlock;
import mindustry.world.draw.*;
import mindustry.world.meta.BuildVisibility;
import tjTool.content.blocks.sandbox.*;
import tjTool.core.*;

import static mindustry.type.ItemStack.*;

public class ThisBlocks {

    public static Block
            anySource, ammoSource,
            solarSource, bedrock, beacon,
            mendSource, overdriveSource, regenSource, shieldSource;

    public static void load() {

        anySource = new AnySource("any-source") {{
            staticRequirements(this, Category.distribution);
        }};

        ammoSource = new AmmoSource("ammo-source") {{
            staticRequirements(this, Category.distribution);
        }};

        solarSource = new SolarSource("solar-source") {{
            staticRequirements(this, Category.power);
        }};

        bedrock = new Bedrock("bedrock") {{
            staticRequirements(this, Category.defense);
        }};

        beacon = new Beacon("beacon") {{
            staticRequirements(this, Category.distribution);
        }};

        mendSource = new MendProjector("mend-source") {
            {
                staticRequirements(this, Category.effect);
                hasPower = false;
                hasItems = false;
                size = 2;
                reload = 250f;
                range = 85f;
                healPercent = 11f;
                phaseBoost = 15f;
                scaledHealth = 80;
            }

            @Override
            public void setStats() {
                super.setStats();
                stats.add(TjStat.config, TjStat.acknowledgements(region));
            }
        };

        overdriveSource = new OverdriveProjector("overdrive-source") {
            {
                staticRequirements(this, Category.effect);
                hasPower = false;
                hasItems = false;
                hasBoost = false;
                lightRadius = 50f;
                size = 3;
                health = 360;
                range = 200f;
                speedBoost = 2.5f;
                useTime = 300f;
            }

            @Override
            public void setStats() {
                super.setStats();
                stats.add(TjStat.config, TjStat.acknowledgements(region));
            }
        };

        regenSource = new RegenProjector("regen-source") {
            {
                staticRequirements(this, Category.effect);
                hasPower = false;
                hasItems = false;
                hasLiquids = false;
                size = 3;
                range = 28;
                baseColor = Pal.regen;
                healPercent = 4f / 60f;
                Color col = Color.valueOf("8ca9e8");
                drawer = new DrawMulti(
                        new DrawRegion("-bottom"),
                        new DrawLiquidTile(Liquids.hydrogen, 9f / 4f) {
                            @Override
                            public void draw(Building build) {
                                LiquidBlock.drawTiledFrames(build.block.size, build.x, build.y, this.padLeft, this.padRight, this.padTop, this.padBottom, this.drawLiquid, this.alpha);
                            }
                        },
                        new DrawDefault(),
                        new DrawGlowRegion() {{
                            color = Color.sky;
                        }},
                        new DrawPulseShape(false) {{
                            layer = Layer.effect;
                            color = col;
                        }},
                        new DrawShape() {{
                            layer = Layer.effect;
                            radius = 3.5f;
                            useWarmupRadius = true;
                            timeScl = 2f;
                            color = col;
                        }}
                );
            }

            @Override
            public void setStats() {
                super.setStats();
                stats.add(TjStat.config, TjStat.acknowledgements(region));
            }
        };

        shieldSource = new ShieldSource("shield-source") {{
            staticRequirements(this, Category.effect);
        }};

    }

    public static void staticRequirements(Block block, Category cat) {
        block.requirements(cat, BuildVisibility.sandboxOnly, with());
        block.alwaysUnlocked = true;
        block.placeableLiquid = true;
    }

}
