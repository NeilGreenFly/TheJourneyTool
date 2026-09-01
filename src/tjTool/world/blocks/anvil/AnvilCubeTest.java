package tjTool.world.blocks.anvil;

import arc.graphics.g2d.Draw;
import tjTool.core.TjCube;

import static tjTool.core.TjFunc.forRange;

@SuppressWarnings("unused")
public class AnvilCubeTest extends Anvil {
    public AnvilCubeTest(String name) {
        super(name);
        update = true;
    }

    @SuppressWarnings("unused")
    public class AnvilCubeTestBuild extends AnvilBuild {
        private final TjCube cube = new TjCube().setScale(10f);

        @Override
        public void created() {
            cube.rotationSpeed.set(1f, 0.5f, 0);
            forRange(2, x -> forRange(2, y -> forRange(2, z -> cube.add(x * 2 - 1, y * 2 - 1, z * 2 - 1))));
            cube.edges.addAll(new int[]{0, 4}, new int[]{1, 5}, new int[]{2, 6}, new int[]{3, 7}, new int[]{0, 2}, new int[]{1, 3}, new int[]{4, 6}, new int[]{5, 7}, new int[]{0, 1}, new int[]{2, 3}, new int[]{4, 5}, new int[]{6, 7});
            cube.areas.addAll(
                    new int[]{0, 2, 6, 4},
                    new int[]{1, 5, 7, 3},
                    new int[]{0, 4, 5, 1},
                    new int[]{2, 3, 7, 6},
                    new int[]{0, 1, 3, 2},
                    new int[]{4, 6, 7, 5}
            );
        }

        @Override
        public void draw() {
            var z = Draw.z();
            super.draw();
            Draw.z(z);
            cube.draw(region);
        }

        @Override
        public void updateTile() {
            cube.setPosition(x, y).update();
        }
    }
}
