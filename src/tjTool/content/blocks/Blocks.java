package tjTool.content.blocks;

import mindustry.world.Block;
import mindustry.world.draw.*;
import tjTool.core.TjBundle;
import tjTool.world.blocks.defense.*;
import tjTool.world.blocks.distribution.*;
import tjTool.world.blocks.production.*;
import tjTool.world.consumers.MultiConsumer;

import static mindustry.content.Items.*;
import static mindustry.content.Liquids.*;
import static mindustry.type.Category.*;
import static mindustry.type.ItemStack.mult;
import static mindustry.world.meta.BuildVisibility.*;
import static tjTool.world.consumers.MultiStack.with;
import static tjTool.world.consumers.SingleConsumer.by;

public class Blocks {

    public static Block
            multiSorter,
            mendWall, mendWallLarge,
            chargedLyreWall,
            multiCrafter;

    public static void load() {

        multiSorter = new MultiSorter("multi-sorter") {{
            staticInit(this);
            requirements(distribution, with(copper, 2, lead, 2));
        }};

        mendWall = new MendWall("mend-wall") {{
            staticInit(this);
            requirements(defense, with(titanium, 4));
            size = 1;
            health = 1200;
            range = 14f;
        }};

        mendWallLarge = new MendWall("mend-wall-large") {{
            staticInit(this);
            requirements(defense, mult(mendWall.requirements, 4f));
            size = 2;
            health = mendWall.health * 4;
            range = 28f;
        }};

        chargedLyreWall = new EdgeWall("charged-lyre-wall") {{
            staticInit(this);
            requirements(defense, with(surgeAlloy, 32));
            health = 1600;
        }};

        multiCrafter = new MultiCrafter("multi-crafter") {{
            staticInit(this);
            requirements(crafting, with(beryllium, 1));
            size = 3;
            drawer = new DrawMulti(new DrawDefault(), new DrawHeatInput(), new DrawFlame(), new DrawCrucibleFlame());
            multiConsumers = new MultiConsumer(
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
            );
        }};

    }

    public static void staticInit(Block block) {
        block.alwaysUnlocked = true;
        block.buildVisibility = sandboxOnly;
        block.details = TjBundle.details(block, "test");
    }

}
