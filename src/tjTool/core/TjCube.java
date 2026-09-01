package tjTool.core;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Vec3;
import arc.struct.Seq;
import arc.util.Tmp;

import static arc.graphics.Color.whiteFloatBits;

@SuppressWarnings("unused")
public class TjCube {
    protected static final Vec3 cos = new Vec3();
    protected static final Vec3 sin = new Vec3();

    public Vec3 position = Vec3.Zero.cpy();
    public Vec3 rotation = Vec3.Zero.cpy();
    public Vec3 rotationSpeed = Vec3.Zero.cpy();
    public float scale;
    public float focalLength;
    public Seq<Vertices> vertices = new Seq<>();
    public Seq<int[]> edges = new Seq<>();
    public Seq<int[]> areas = new Seq<>();

    public TjCube() {
        scale = 20f;
        focalLength = 100f;
    }

    public TjCube setPosition(float x, float y) {
        position.set(x, y, 0);
        return this;
    }

    public TjCube setScale(float scale) {
        this.scale = scale;
        return this;
    }

    public void add(float x, float y, float z) {
        vertices.add(new Vertices(x, y, z));
    }

    public void update() {
        rotation.add(rotationSpeed);
        cos.set(Mathf.cosDeg(rotation.x), Mathf.cosDeg(rotation.y), Mathf.cosDeg(rotation.z));
        sin.set(Mathf.sinDeg(rotation.x), Mathf.sinDeg(rotation.y), Mathf.sinDeg(rotation.z));
        for (var v : vertices) v.project3DTo2D();
    }

    public void drawEdge() {
        Vertices v0, v1;
        Lines.stroke(3f);
        for (var edge : edges) {
            v0 = vertices.get(edge[0]);
            v1 = vertices.get(edge[1]);
            Lines.line(
                    v0.projectTo.x + position.x,
                    v0.projectTo.y + position.y,
                    v1.projectTo.x + position.x,
                    v1.projectTo.y + position.y
            );
        }
    }

    public void draw(TextureRegion region) {
        for (var v : areas) {
            var v0 = vertices.get(v[0]);
            var v1 = vertices.get(v[1]);
            var v2 = vertices.get(v[2]);
            var v3 = vertices.get(v[3]);
            if (!Vertices.shouldDraw(v0, v1, v2)) continue;
            Draw.quad(region,
                    v0.projectTo.x + position.x,
                    v0.projectTo.y + position.y,
                    whiteFloatBits,
                    v1.projectTo.x + position.x,
                    v1.projectTo.y + position.y,
                    whiteFloatBits,
                    v2.projectTo.x + position.x,
                    v2.projectTo.y + position.y,
                    whiteFloatBits,
                    v3.projectTo.x + position.x,
                    v3.projectTo.y + position.y,
                    whiteFloatBits
            );
        }
    }

    public class Vertices {
        public float x, y, z;
        public Vec3 projectTo = Vec3.Zero.cpy();

        public Vertices(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public void project3DTo2D() {
            projectTo.set(x, y, z).set(
                    projectTo.x,
                    projectTo.y * cos.x - projectTo.z * sin.x,
                    projectTo.y * sin.x + projectTo.z * cos.x
            ).set(
                    projectTo.x * cos.y + projectTo.z * sin.y,
                    projectTo.y,
                    -projectTo.x * sin.y + projectTo.z * cos.y
            ).set(
                    projectTo.x * cos.z - projectTo.y * sin.z,
                    projectTo.x * sin.z + projectTo.y * cos.z,
                    projectTo.z
            ).set(
                    projectTo.x * scale,
                    projectTo.y * scale,
                    projectTo.z * scale
            );
            float scale3D = focalLength / (focalLength + projectTo.z);
            projectTo.set(
                    projectTo.x * scale3D,
                    projectTo.y * scale3D,
                    projectTo.z
            );
        }

        public static boolean shouldDraw(Vertices v0, Vertices v1, Vertices v2) {
            return Tmp.v31.set(
                    v1.projectTo.x - v0.projectTo.x,
                    v1.projectTo.y - v0.projectTo.y,
                    v1.projectTo.z - v0.projectTo.z
            ).crs(
                    v2.projectTo.x - v1.projectTo.x,
                    v2.projectTo.y - v1.projectTo.y,
                    v2.projectTo.z - v1.projectTo.z
            ).nor().dot(Vec3.Z) < 0;
        }
    }
}
