package tjTool.core;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.math.geom.Vec3;
import arc.struct.Seq;
import arc.util.Tmp;

import static tjTool.core.TjCube.Vertices.*;
import static tjTool.world.LazyGetter.*;

@SuppressWarnings("unused")
public class TjCube {
    protected static final Vec3 cos = new Vec3();
    protected static final Vec3 sin = new Vec3();
    protected static final float[] hsv = new float[3];
    protected static float minValue = 40;
    protected static float from = -20;
    protected static float to = 10;

    public static boolean drawFront = true;
    public static float multiplier = 1;
    public static float alpha = 1;
    public static float z = 1.1f;

    public Vec2 position = new Vec2();
    public Vec3 rotation = new Vec3();
    public Vec3 rotationSpeed = new Vec3();
    public float scale;
    public float focalLength;
    public Seq<Vertices> vertices = new Seq<>();
    public Seq<int[]> edges = new Seq<>();
    public Seq<int[]> areas = new Seq<>();

    public TjCube() {
        scale = 20f;
        focalLength = 100f;
    }

    public TjCube at(float x, float y) {
        position.set(x, y);
        return this;
    }

    public TjCube at(Position v) {
        position.set(v);
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

    public TjCube config(float multi, float a, boolean front) {
        multiplier = multi;
        alpha = a;
        drawFront = front;
        return this;
    }

    public void hsv(float h, float s, float v) {
        hsv[0] = h;
        hsv[1] = s;
        hsv[2] = v;
    }

    public void fill(Color color) {
        color.toHsv(hsv);
        hsv[1] *= 100;
        hsv[2] *= 100;
        drawArea(Core.atlas.white());
    }

    public void draw(TextureRegion region) {
        hsv(0, 0, 100);
        drawArea(region);
    }

    public void drawArea(TextureRegion region) {
        var h = getOffsetByHeight(position.x, position.y, z);
        for (var v : areas) {
            var v0 = vertices.get(v[0]);
            var v1 = vertices.get(v[1]);
            var v2 = vertices.get(v[2]);
            var v3 = vertices.get(v[3]);
            if (shouldDraw(v0, v1, v2)) Fill.quad(region,
                    v0.projectTo.x * multiplier + h.x,
                    v0.projectTo.y * multiplier + h.y, vecHsv(v0, Tmp.c1),
                    v1.projectTo.x * multiplier + h.x,
                    v1.projectTo.y * multiplier + h.y, vecHsv(v1, Tmp.c2),
                    v2.projectTo.x * multiplier + h.x,
                    v2.projectTo.y * multiplier + h.y, vecHsv(v2, Tmp.c3),
                    v3.projectTo.x * multiplier + h.x,
                    v3.projectTo.y * multiplier + h.y, vecHsv(v3, Tmp.c4));
        }
    }

    public static float vecHsv(Vertices v, Color c) {
        return Color.HSVtoRGB(hsv[0], hsv[1], Mathf.map(v.projectTo.z, from, to, minValue, hsv[2]), c.a(alpha)).toFloatBits();
    }

    public class Vertices {
        public float x, y, z;
        public Vec3 projectTo = new Vec3();

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
                    -projectTo.z
            );
        }

        public static boolean shouldDraw(Vertices v0, Vertices v1, Vertices v2) {
            return drawFront == Tmp.v31.set(
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
