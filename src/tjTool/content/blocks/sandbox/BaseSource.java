package tjTool.content.blocks.sandbox;

import mindustry.world.blocks.heat.HeatBlock;
import mindustry.world.meta.BlockGroup;

public class BaseSource extends SandboxBlock {
    public float powerProduction = 1000000f / 60f;
    public float heatOutput = 1000f;

    public BaseSource(String name) {
        super(name);
        hasPower = true;
        outputsPower = true;
        consumesPower = false;
        conductivePower = true;
        group = BlockGroup.transportation;
    }

    @SuppressWarnings("unused")
    public class BaseSourceBuild extends SandboxBuild implements HeatBlock {

        @Override
        public float getPowerProduction() {
            return powerProduction;
        }

        @Override
        public float heat() {
            return heatOutput;
        }

        @Override
        public float heatFrac() {
            return 1f;
        }

    }
}
