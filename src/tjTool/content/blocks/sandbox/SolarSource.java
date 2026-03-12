package tjTool.content.blocks.sandbox;

import mindustry.world.blocks.power.SolarGenerator;

import static mindustry.world.blocks.power.PowerNode.makeBatteryBalance;
import static mindustry.world.blocks.power.PowerNode.makePowerBalance;

public class SolarSource extends SolarGenerator {
    public SolarSource(String name) {
        super(name);
        size = 1;
        health = 40;
        canOverdrive = false;
        placeableLiquid = true;
        powerProduction = 1000000f / 60f;
    }

    @Override
    public void setBars() {
        super.setBars();
        removeBar("power");
        addBar("power", makePowerBalance());
        addBar("batteries", makeBatteryBalance());
    }

    public class SolarSourceBuild extends GeneratorBuild {
        @Override
        public void updateTile() {
            productionEfficiency = 1f;
        }
    }
}
