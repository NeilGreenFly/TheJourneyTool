package tjTool.world.graphics;

import arc.math.geom.Mat3D;
import mindustry.graphics.Shaders.LoadShader;

/**
 * @see mindustry.graphics.Shaders
 */
public class Shaders {

    public static AnvilRingShader shader = new AnvilRingShader();

    public static class AnvilRingShader extends LoadShader {
        public Mat3D camera;
        public Mat3D model;

        public AnvilRingShader() {
            super("anvil", "anvil");
        }

        @Override
        public void apply() {
            setUniformMatrix4("u_proj", camera.val);
            setUniformMatrix4("u_trans", model.val);
        }
    }
}
