package tjTool.content.blocks;

import arc.func.Cons;
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
import tjTool.world.draw.DrawBottom;
import tjTool.world.blocks.sandbox.*;
import tjTool.core.*;

import static mindustry.type.Category.*;
import static mindustry.type.ItemStack.*;
import static mindustry.world.meta.BuildVisibility.sandboxOnly;
import static tjTool.world.LazyGetter.apply;

public class SandboxBlocks {

    public static Block
            anySource, dirSource, anyVoid,
            solarSource, bedrock, beacon,
            mendSource, overdriveSource, regenSource, shieldSource;

    public static void load() {

        anySource = apply(new AnySource("any-source"), staticRequirements(distribution));
        dirSource = apply(new DirSource("dir-source"), staticRequirements(distribution));
        anyVoid = apply(new AnyVoid("any-void"), staticRequirements(distribution));
        solarSource = apply(new SolarSource("solar-source"), staticRequirements(power));
        bedrock = apply(new Bedrock("bedrock"), staticRequirements(defense));
        beacon = apply(new Beacon("beacon"), staticRequirements(effect));

        mendSource = new MendProjector("mend-source") {
            {
                staticRequirements(this, effect);
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

        overdriveSource = apply(new OverdriveSource("overdrive-source"), staticRequirements(effect));

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
                        new DrawBottom(),
                        new DrawLiquidTile(Liquids.hydrogen, 9f / 4f) {
                            @Override
                            public void draw(Building build) {
                                LiquidBlock.drawTiledFrames(build.block.size, build.x, build.y, padLeft, padRight, padTop, padBottom, drawLiquid, alpha);
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
            staticRequirements(this, effect);
        }};

    }

    public static <T extends Block> Cons<T> staticRequirements(Category category) {
        return block -> staticRequirements(block, category);
    }

    public static void staticRequirements(Block block, Category category) {
        block.requirements(category, sandboxOnly, empty);
        block.alwaysUnlocked = true;
        block.placeableLiquid = true;
        block.description = TjBundle.description(block, "sandbox");
        block.details = TjBundle.details(block, "sandbox");
    }

}
