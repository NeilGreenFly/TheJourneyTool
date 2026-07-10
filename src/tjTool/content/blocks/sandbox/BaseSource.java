package tjTool.content.blocks.sandbox;

import arc.func.Boolf;
import mindustry.game.Team;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mindustry.world.blocks.heat.HeatBlock;
import mindustry.world.blocks.payloads.BuildPayload;
import mindustry.world.blocks.payloads.UnitPayload;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.meta.BlockGroup;

import static mindustry.Vars.*;

public abstract class BaseSource extends SandboxBlock {
    protected static BuildPayload[] buildPayloadPool;
    protected static UnitPayload[] unitPayloadPool;

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

    @Override
    public void load() {
        super.load();
        buildPayloadPool = new BuildPayload[content.blocks().size];
        unitPayloadPool = new UnitPayload[content.units().size];
    }

    protected static void payloadPool(Block block, Team team, Boolf<BuildPayload> run) {
        if (buildPayloadPool[block.id] == null) buildPayloadPool[block.id] = new BuildPayload(block, team);
        else buildPayloadPool[block.id].build.team(team);
        if (run.get(buildPayloadPool[block.id])) buildPayloadPool[block.id] = null;
    }

    protected static void payloadPool(UnitType unit, Team team, Boolf<UnitPayload> run) {
        if (unitPayloadPool[unit.id] == null) unitPayloadPool[unit.id] = new UnitPayload(unit.create(team));
        else unitPayloadPool[unit.id].unit.team(team);
        if (run.get(unitPayloadPool[unit.id])) unitPayloadPool[unit.id] = null;
    }

    protected static boolean canProduce(Block block){
        return block.isVisible() && !(block instanceof CoreBlock) && !state.rules.isBanned(block) && block.environmentBuildable();
    }

    protected static boolean canProduce(UnitType unit){
        return !unit.isHidden() && !unit.isBanned() && unit.supportsEnv(state.rules.env);
    }

    @SuppressWarnings("unused")
    public abstract class BaseSourceBuild extends SandboxBuild implements HeatBlock {

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
