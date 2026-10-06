package tjTool.world.graphics;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.Gl;
import arc.graphics.Mesh;
import arc.graphics.g2d.Bloom;
import arc.graphics.g2d.Draw;
import arc.graphics.g3d.Camera3D;
import arc.graphics.gl.FrameBuffer;
import arc.math.geom.Mat3D;
import arc.math.geom.Vec3;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.graphics.g3d.PlanetParams;
import mindustry.type.Planet;

import static arc.Core.graphics;
import static mindustry.graphics.Shaders.screenspace;
import static tjTool.world.graphics.MultiModel.ring;
import static tjTool.world.graphics.MultiModel.ringMesh;
import static tjTool.world.graphics.Shaders.shader;

public class AnvilRingRender {
    public static int graphicsWidth, graphicsHeight;
    public static final Mat3D model = new Mat3D();
    public static final Camera3D camera = new Camera3D();
    public static final Camera3D planetCamera = new Camera3D();
    public static final PlanetParams params = new PlanetParams();
    public static final FrameBuffer buffer = new FrameBuffer(graphics.getWidth(), graphics.getHeight(), true);
    public static final Bloom bloom = new Bloom(graphics.getWidth() / 4, graphics.getHeight() / 4, true, true) {{
        blurPasses = 4;
        setThreshold(0.8f);
        setBloomIntensity(0.2f);
    }};
    public static float z = 2;
    public static float v = 0;

    public static void gl(Runnable drawer) {
        Draw.flush();
        Gl.clear(Gl.depthBufferBit);
        Gl.depthMask(true);
        Gl.enable(Gl.depthTest);
        Gl.enable(Gl.cullFace);
        Gl.cullFace(Gl.back);
        drawer.run();
        Gl.disable(Gl.cullFace);
        Gl.disable(Gl.depthTest);
    }

    public static void fbo(Runnable drawer) {
        buffer.resize(graphicsWidth, graphicsHeight);
        buffer.begin(Color.clear);
        drawer.run();
        buffer.end();
        buffer.blit(screenspace);
    }

    private static void init() {
        graphicsWidth = graphics.getWidth();
        graphicsHeight = graphics.getHeight();
    }

    private static void resetCamera(Camera3D camera) {
        camera.resize(graphicsWidth, graphicsHeight);
        camera.up.set(Vec3.Y);
    }

    private static Vec3 setCamera(Camera3D camera, float x, float y) {
        return camera.position.set(
                (Core.camera.position.x - x) / 20,
                (Core.camera.position.y - y) / 20,
                50 / Vars.renderer.camerascale - z);
    }

    private static void initCamera(float x, float y) {
        resetCamera(camera);
        setCamera(camera, x, y);
        camera.direction.set(0, 0, -1);
    }

    private static void initPlanet(float x, float y, Planet planet) {
        params.viewW = graphicsWidth;
        params.viewH = graphicsHeight;
        params.planet = planet;
        params.uiAlpha = 0;
        resetCamera(planetCamera);
        if (planet.parent == null) {
            planetCamera.direction.set(0, 0, -1);
            setCamera(planetCamera, x, y).add(planet.position);
        } else {
            planetCamera.direction.set(planet.position).sub(planet.parent.position).rotate(Vec3.Y, Time.time * v)
                    .nor().scl(50 / Vars.renderer.camerascale - z);
            Tmp.v31.set(planetCamera.direction).crs(Vec3.Y).nor();
            Tmp.v32.set(Tmp.v31).crs(planetCamera.direction).nor();
            planetCamera.position.set(planet.position).sub(planetCamera.direction)
                    .add(Tmp.v31.scl((Core.camera.position.x - x) / 20))
                    .add(Tmp.v32.scl((Core.camera.position.y - y) / 20));
        }
    }

    public static void render(float x, float y, Planet planet) {
        init();
        initCamera(x, y);
        initPlanet(x, y, planet);
        planetCamera.update();

//        gl(() -> fbo(() -> {
//            camera.near = camera.position.z - z;
//            camera.far = 100;
//            camera.update();
//            renderRing();
//            renderPlanet(planet);
//            camera.near = 1;
//            camera.far = camera.position.z - z;
//            camera.update();
//            renderRing();
//        }));

        camera.near = camera.position.z - 1;
        camera.far = 100;
        camera.update();
        gl(() -> {
            fbo(AnvilRingRender::renderRing);
            planet.draw(params, planetCamera.combined, planet.getTransform(model));
        });
        gl(() -> fbo(() -> renderPlanet(planet)));
        camera.near = 1;
        camera.far = camera.position.z;
        camera.update();
        gl(() -> fbo(AnvilRingRender::renderRing));
    }

    private static void renderRing() {
        shader.bind();
        ring.bind();
        shader.camera = camera.combined;

        shader.model = model.idt().scale(2, 2, 2)
                .rotate(Vec3.X, Time.time * 0.25f)
                .rotate(Vec3.Y, Time.time * 0.5f);
        shader.apply();
        ringMesh.render(shader, Gl.triangles);

        shader.model = model.idt().scale(3.2f, 3.2f, 3)
                .rotate(Vec3.X, Time.time * 0.25f)
                .rotate(Vec3.Y, Time.time * 0.25f);
        shader.apply();
        ringMesh.render(shader, Gl.triangles);
    }

    /**
     * @param planet planet
     * @see Planet#draw(PlanetParams, Mat3D, Mat3D)
     * @see Planet#drawClouds(PlanetParams, Mat3D, Mat3D)
     * @see Planet#drawAtmosphere(Mesh, Camera3D)
     */
    private static void renderPlanet(Planet planet) {
        planet.draw(params, planetCamera.combined, planet.getTransform(model));
        bloom.resize(params.viewW, params.viewH);
        bloom.capture();
        planet.drawClouds(params, planetCamera.combined, planet.getTransform(model));
        if (planet.hasAtmosphere && planet.parent != null && planetCamera.frustum.containsSphere(planet.position, planet.clipRadius))
            planet.drawAtmosphere(Vars.renderer.planets.atmosphere, planetCamera);
        bloom.render();
    }
}
