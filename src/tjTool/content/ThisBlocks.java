package tjTool.content;

import mindustry.type.Category;
import mindustry.world.Block;
import mindustry.world.blocks.defense.OverdriveProjector;
import mindustry.world.meta.BuildVisibility;
import tjTool.content.blocks.sandbox.*;

import static mindustry.type.ItemStack.*;

public class ThisBlocks {

    public static Block anySource, ammoSource, solarSource, overdriveSource;

    public static void load() {

        anySource = new AnySource("any-source") {{
            requirements(Category.distribution);
        }};

        ammoSource = new AmmoSource("ammo-source") {{
            requirements(Category.distribution);
        }};

        solarSource = new SolarSource("solar-source") {{
            requirements(Category.power, BuildVisibility.sandboxOnly, with());
            alwaysUnlocked = true;
        }};

        overdriveSource = new OverdriveProjector("overdrive-source") {{
            hasPower = false;
            hasItems = false;
            hasBoost = false;
            lightRadius = 50f;
            size = 3;
            health = 360;
            range = 200f;
            speedBoost = 2.5f;
            useTime = 300f;
            placeableLiquid = true;
            requirements(Category.effect, BuildVisibility.sandboxOnly, with());
            alwaysUnlocked = true;
        }};

    }
}
