package tjTool.content.blocks;

import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.world.Block;
import mindustry.world.draw.*;
import tjTool.world.blocks.defense.*;
import tjTool.world.blocks.distribution.*;
import tjTool.world.consumers.MultiConsumer;
import tjTool.world.blocks.production.*;

import static mindustry.content.Items.*;
import static mindustry.content.Liquids.*;
import static mindustry.type.Category.*;
import static mindustry.type.ItemStack.mult;
import static mindustry.type.ItemStack.with;
import static mindustry.world.meta.BuildVisibility.*;

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
            requirements(defense, ItemStack.with(surgeAlloy, 32));
            health = 1600;
        }};

        multiCrafter = new MultiCrafter("multi-crafter") {{
            staticInit(this);
            requirements(crafting, with(beryllium, 1));
            size = 3;
            drawer = new DrawMulti(new DrawDefault(), new DrawHeatInput(), new DrawFlame(), new DrawCrucibleFlame());
            multiConsumers = new MultiConsumer[]{
                    MultiConsumer.by(with(copper, 3, lead, 4, titanium, 2, silicon, 3), null, with(surgeAlloy, 4), null).timePerSec(1.25f).powerPerSec(240),
                    MultiConsumer.by(with(beryllium, 1), LiquidStack.with(ozone, 2 / 60f), with(oxide, 2), null).timePerSec(2).powerPerSec(30),
                    MultiConsumer.by(with(tungsten, 2, graphite, 3), null, with(carbide, 2), null).timePerSec(2.25f / 4f).powerPerSec(120).heat(40),
                    MultiConsumer.by(with(thorium, 2, sand, 6), LiquidStack.with(ozone, 8 / 60f), with(phaseFabric, 2, fissileMatter, 1), null).timePerSec(0.5f).powerPerSec(480).heat(32),
                    MultiConsumer.by(null, LiquidStack.with(cryofluid, 12 / 60f), with(titanium, 1), LiquidStack.with(water, 12 / 60f, nitrogen, 36 / 60f)).timePerSec(2)//.powerPerSec(60),
            };
        }};

    }

    public static void staticInit(Block block) {
        block.alwaysUnlocked = true;
        block.buildVisibility = hidden;
    }

}
