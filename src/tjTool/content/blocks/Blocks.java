package tjTool.content.blocks;

import mindustry.world.Block;
import mindustry.world.draw.*;
import tjTool.core.TjBundle;
import tjTool.world.blocks.liquid.*;
import tjTool.world.blocks.defense.*;
import tjTool.world.blocks.distribution.*;
import tjTool.world.blocks.production.*;
import tjTool.world.consumers.*;

import static mindustry.content.Items.*;
import static mindustry.content.Liquids.*;
import static mindustry.type.Category.*;
import static mindustry.type.ItemStack.mult;
import static mindustry.world.meta.BuildVisibility.*;
import static tjTool.world.LazyGetter.apply;
import static tjTool.world.consumers.MultiStack.with;
import static tjTool.world.consumers.OptionalConsumer.by;
import static tjTool.world.consumers.SingleConsumer.by;

/**
 * @see mindustry.content.Blocks
 */
@SuppressWarnings("SpellCheckingInspection")
public class Blocks {

    public static Block
            multiSorter, plastaniumJunction,
            dirLiquidRouter, liquidUnloader, dirLiquidUnloader,
            mendWall, mendWallLarge,
            chargedLyreWall,
            multiCrafter;

    public static void load() {

        multiSorter = apply(new MultiSorter("multi-sorter"), it -> {
            staticInit(it);
            it.requirements(distribution, with(copper, 2, lead, 2));
        });

        plastaniumJunction = apply(new StackJunction("plastanium-junction"), it -> {
            staticInit(it);
            it.requirements(distribution, with(graphite, 4, silicon, 2, plastanium, 2));
            it.health = 160;
            it.speed = 4f / 60f;
            it.itemCapacity = 10;
        });

        dirLiquidRouter = apply(new DirLiquidRouter("dir-liquid-router"), it -> {
            staticInit(it);
            it.requirements(liquid, with(beryllium, 4, graphite, 10));
            it.health = 300;
        });

        liquidUnloader = apply(new LiquidUnloader("liquid-unloader"), it -> {
            staticInit(it);
            it.requirements(liquid, with(metaglass, 10, titanium, 20, silicon, 30));
        });

        dirLiquidUnloader = apply(new DirLiquidUnloader("dir-liquid-unloader"), it -> {
            staticInit(it);
            it.requirements(liquid, with(beryllium, 10, graphite, 20, silicon, 20, tungsten, 10));
            it.health = 250;
        });

        // multiConduit = new MultiConduit("multi-conduit") {{
        //     staticInit(this);
        //     requirements(liquid, with(beryllium, 16));
        //     buildVisibility = debugOnly;
        // }};

        mendWall = apply(new MendWall("mend-wall"), it -> {
            staticInit(it);
            it.requirements(defense, with(titanium, 4));
            it.size = 1;
            it.health = 1200;
            it.range = 14f;
        });

        mendWallLarge = apply(new MendWall("mend-wall-large"), it -> {
            staticInit(it);
            it.requirements(defense, mult(mendWall.requirements, 4f));
            it.size = 2;
            it.health = mendWall.health * 4;
            it.range = 28f;
        });

        chargedLyreWall = apply(new EdgeWall("charged-lyre-wall"), it -> {
            staticInit(it);
            it.requirements(defense, with(surgeAlloy, 32));
            it.health = 1600;
        });

        multiCrafter = apply(new MultiCrafter("multi-crafter"), it -> {
            staticInit(it);
            it.requirements(crafting, with(beryllium, 1));
            it.size = 3;
            it.drawer = new DrawMulti(new DrawDefault(), new DrawHeatInput(), new DrawFlame(), new DrawCrucibleFlame());
            it.multiConsumers = new MultiConsumer(
                    by(1.25f)
                            .inputBy(with(copper, 3, lead, 4, titanium, 2, silicon, 3), null)
                            .outputBy(with(surgeAlloy, 4), null)
                            .powerPerSec(240),
                    by(2)
                            .inputBy(with(beryllium, 1), with(ozone, 2 / 60f))
                            .outputBy(with(oxide, 2), null)
                            .powerPerSec(30),
                    by(2.25f / 4f)
                            .inputBy(with(tungsten, 2, graphite, 3), null)
                            .outputBy(with(carbide, 2), null)
                            .powerPerSec(120)
                            .heat(40),
                    by(0.5f)
                            .inputBy(with(thorium, 2, sand, 6), with(ozone, 8 / 60f))
                            .outputBy(with(phaseFabric, 2, fissileMatter, 1), null)
                            .powerPerSec(480)
                            .heat(32),
                    by(2)
                            .inputBy(null, with(cryofluid, 12 / 60f))
                            .outputBy(with(titanium, 1), with(water, 12 / 60f, nitrogen, 36 / 60f)) //.powerPerSec(60)
            ).optional(
                    by(null, with(cyanogen, 2 / 60f))
            );
        });

    }

    public static void staticInit(Block block) {
        block.alwaysUnlocked = true;
        block.buildVisibility = debugOnly; // 会被 requirements(...) 方法覆盖, 不过现阶段无所谓我以后会来调整的.
        block.details = TjBundle.details(block, "test");
    }

}
