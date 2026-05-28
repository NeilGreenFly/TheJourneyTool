package tjTool.core;

import arc.func.Boolf3;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.world.Block;

import static arc.Core.camera;
import static arc.math.geom.Geometry.*;
import static mindustry.Vars.tilesize;

@SuppressWarnings("unused")
public class TjDraw {

    public static float z = 2f;
    public static Color rainbow = new Color();

    public static void update() {
        rainbow = rainbow(25f, 100f);
    }

    public static String colorToString(Color color) {
        return "[#" + color + "]";
    }

    public static Color rainbow() {
        return rainbow;
    }

    public static Color rainbow(float s, float v) {
        return Color.HSVtoRGB(Time.time % 360, s, v);
    }

    public static String rainbowStream(String string) {
        StringBuilder rainbowString = new StringBuilder();
        for (int i = 0; i < string.length(); i += 1)
            rainbowString.append(TjDraw.colorToString(Color.HSVtoRGB((Time.time + i) * 3 % 360f, 25f, 100f))).append(string.charAt(i));
        return rainbowString.toString();
    }

    public static String flashingStream(String string, Color color, Color colorTo) {
        StringBuilder rainbowString = new StringBuilder();
        for (int i = 0; i < string.length(); i += 1)
            rainbowString.append(TjDraw.colorToString(color.cpy().lerp(colorTo, Math.abs((Time.time + i) % 200 - 100) / 100))).append(string.charAt(i));
        return rainbowString.toString();
    }

    public static void arcCircle(float x, float y, float radius, int sectors, float fraction, float rotateSpeed) {
        for (int i = 0; i < sectors; i += 1)
            Lines.arc(x, y, radius, fraction, i * 360f / sectors - Time.time * rotateSpeed);
    }

    public static void lightPoly(Building building) {
        lightPoly(building, 4, building.team.color);
    }

    public static void lightPoly(Building building, Color color) {
        lightPoly(building, 4, color);
    }

    public static void lightPoly(Building building, int sides, Color color) {
        lightPoly(building.x, building.y, sides, building.block.size * tilesize, color);
    }

    public static void lightPoly(float x, float y, int sides, float radius, Color color) {
        Fill.lightInner(x, y, sides,
                Math.max(0f, radius * 0.6f),
                radius,
                0f,
                Tmp.c3.set(color).a(0f),
                Tmp.c2.set(color).a(0.7f)
        );
        Lines.stroke(1f);
        Draw.color(color);
        Lines.poly(x, y, sides, radius + 0.5f);
        Draw.reset();
    }

    public static void overdrive(Building building, String hex, float heat) {
        overdrive(building, hex, hex, heat);
    }

    public static void overdrive(Building building, String hex, String hexTo, float heat) {
        float f = 1f - (Time.time / 100f) % 1f;
        float r = Math.max(0f, Mathf.clamp(2f - f * 2f) * building.block.size * tilesize / 2f - f - 0.2f);
        float w = Mathf.clamp(0.5f - f) * building.block.size * tilesize;

        Draw.color(Color.valueOf(hexTo), Color.valueOf(hex), f);
        Draw.alpha(heat * Mathf.absin(Time.time, 50f / Mathf.PI2, 1f) * 0.5f);
        Draw.alpha(1f);
        Lines.stroke((2f * f + 0.1f) * heat);

        Lines.beginLine();
        for (int i = 0; i < 4; ++i) {
            Lines.linePoint(
                    building.x + d4(i).x * r + d4(i).y * w,
                    building.y + d4(i).y * r - d4(i).x * w);
            if(f < 0.5f) Lines.linePoint(
                    building.x + d4(i).x * r - d4(i).y * w,
                    building.y + d4(i).y * r + d4(i).x * w);
        }
        Lines.endLine(true);

        Draw.reset();
    }

    /**
     * Draw reference lines when placing.
     * For example:
     * <blockquote><pre>
     *     {@code @Override}
     *     public void drawPlace(int x, int y, int rotation, boolean valid) {
     *         super.drawPlace(x, y, rotation, valid);
     *         TjDraw.drawPlace(this, x, y, valid);
     *     }
     * </pre></blockquote>
     * @param block The block which is placing
     * @param x     x
     * @param y     y
     * @param valid Whether it is allowed to place this block here
     * @author NeilGreenFly
     * @see mindustry.world.Block#drawPlace(int, int, int, boolean)
     */
    public static void drawPlace(Block block, int x, int y, boolean valid) {
        float width = 2;
        float[] r = new float[]{block.size * tilesize / 2f, 0, tilesize * 16};
        float[] wr = new float[]{width / 2f, block.size * tilesize / 2f - width};
        float cx = x * tilesize + block.offset;
        float cy = y * tilesize + block.offset;
        Color color = valid ? rainbow : Pal.remove.cpy();
        float outline = color.a(0.5f).toFloatBits();
        float from = color.a(0.25f).toFloatBits();
        float to = color.a(0).toFloatBits();
        Draw.color(color);
        Draw.alpha(0.35f);
        for (int i = 0; i < 4; i += 1) {
            for (int j = 0; j < 3; j += 1) {
                float ccx = j % 2 == 1 ? cx : cx + (block.size * tilesize - width) / 2f * d4(i + j - 1).x;
                float ccy = j % 2 == 1 ? cy : cy + (block.size * tilesize - width) / 2f * d4(i + j - 1).y;
                r[1] = wr[j % 2];
                float c = j % 2 == 1 ? from : outline;
                Fill.quad(
                        ccx + r[i % 2] * d8edge(i).x,
                        ccy + r[(i + 1) % 2] * d8edge(i).y, c,
                        ccx + r[i % 2] * d8edge(i - 1).x,
                        ccy + r[(i + 1) % 2] * d8edge(i - 1).y, c,
                        ccx + r[(i + 1) % 2 + 1] * d8edge(i - 1).x,
                        ccy + r[i % 2 + 1] * d8edge(i - 1).y, to,
                        ccx + r[(i + 1) % 2 + 1] * d8edge(i).x,
                        ccy + r[i % 2 + 1] * d8edge(i).y, to);
            }
            Fill.rect(
                    cx + (r[0] + wr[0]) * d4(i).x,
                    cy + (r[0] + wr[0]) * d4(i).y,
                    wr[i % 2] * 2,
                    wr[(i + 1) % 2] * 2);
        }
        Draw.color();
    }

    public static void drawPlace(Block block, int x, int y) {
        float[] r = new float[]{block.size * tilesize / 2f, tilesize * 16};
        float cx = x * tilesize + block.offset;
        float cy = y * tilesize + block.offset;
        Color color = rainbow;
        float from = color.a(0.25f).toFloatBits();
        float to = color.a(0).toFloatBits();
        for (int i = 0; i < 4; ++i)
            Fill.quad(
                    cx + r[0] * d8edge(i).x, cy + r[0] * d8edge(i).y, from,
                    cx + r[0] * d8edge(i - 1).x, cy + r[0] * d8edge(i - 1).y, from,
                    cx + r[(i + 1) % 2] * d8edge(i - 1).x, cy + r[i % 2] * d8edge(i - 1).y, to,
                    cx + r[(i + 1) % 2] * d8edge(i).x, cy + r[i % 2] * d8edge(i).y, to);
    }

    public static void beacon(Building building, Color color, TextureRegion icon) {
        Boolf3<Float, Float, Integer> b = (x, y, i) -> {
            // Building building = world.buildWorld(x, y);
            Building other = null;
            if (building != null) other = building.nearby(d4x[i] * (building.block.size / 2 + 1), d4y[i] * (building.block.size / 2 + 1));
            return (building == null || other == null || !(building.block == other.block &&
                    building.x + building.block.size * tilesize * d4x[i] == other.x &&
                    building.y + building.block.size * tilesize * d4y[i] == other.y));
        };
        beacon(building.x, building.y, building.block.size * tilesize / 2f, color, 0.7f, b, icon);
    }

    public static void beacon(float x, float y, float r, Color color, Float alpha) {
        beacon(x, y, r, color, alpha, null, null);
    }

    /**
     * 绘制一个信标.
     * <p>
     * 您可以像这样绘制一个信标 :
     * <blockquote><pre>
     *     {@code @Override}
     *     public void draw() {
     *         super.draw();
     *         float r = size * tilesize / 2f;
     *         beacon(x, y, r, team.color.cpy(), 0.7f, null, null);
     *     }
     * </pre></blockquote>
     * @param x     中心坐标 x
     * @param y     中心坐标 y
     * @param r     半径
     * @param color 颜色
     * @param alpha alpha 通道最大值
     * @param b     是否绘制当前面
     * @param icon  图标投影
     * @author NeilGreenFly
     * @see mindustry.world.Block#drawPlace(int, int, int, boolean)
     * @see arc.func.Boolf3
     */
    public static void beacon(float x, float y, float r, Color color, float alpha, Boolf3<Float, Float, Integer> b, TextureRegion icon) {
        float dx = camera.position.x - x;
        float dy = camera.position.y - y;
        float hx = camera.position.x - dx * z;
        float hy = camera.position.y - dy * z;
        float hr = r * z;
        float len = (Mathf.len(dx, dy) - tilesize * 16) / 128f;
        float a = Math.min(len, alpha);
        float from = color.a(Mathf.clamp(a)).toFloatBits();
        float to = color.a(0f).toFloatBits();
        Draw.z(Layer.effect - 1); // Layer.flyingUnit + 1   Layer.blockOver
        for (int i = 0; i < 4; i += 1) {
            if ((d4x[i] * dx > r || d4y[i] * dy > r) && (b == null || b.get(x, y, i)))
                Fill.quad(
                        x + r * d8edge(i).x,
                        y + r * d8edge(i).y, from,
                        x + r * d8edge(i - 1).x,
                        y + r * d8edge(i - 1).y, from,
                        hx + hr * d8edge(i - 1).x,
                        hy + hr * d8edge(i - 1).y, to,
                        hx + hr * d8edge(i).x,
                        hy + hr * d8edge(i).y, to
                );
        }
        if (icon != null) {
            float iconSize = 320;
            float d = z * 0.95f;
            Draw.z(Layer.effect);
            Lines.stroke(32);
            Draw.color(color.a(Mathf.clamp(a, 0f, 0.3f)));
            Lines.circle(camera.position.x - dx * d, camera.position.y - dy * d, iconSize);
            Draw.color(color.a(Mathf.clamp(a, 0f, 0.5f)));
            arcCircle(camera.position.x - dx * d, camera.position.y - dy * d, iconSize + 64, 6, 0.1f, 0.5f);
            d = z * 0.9f;
            Draw.color(color.a(Mathf.clamp(a, 0f, 0.25f)));
            arcCircle(camera.position.x - dx * d, camera.position.y - dy * d, iconSize / 2, 3, 0.16f, -0.5f);
            Draw.color();
            Draw.alpha(len);
            Draw.rect(icon, hx, hy, iconSize, iconSize);
        }
    }

    public static void drawProximity(int x, int y, int size, Color color) {
        Draw.color(color);
        Draw.alpha(0.5f);
        Fill.square((x - 1) * tilesize, y * tilesize, 2 * size);
        Fill.square((x + 1) * tilesize, y * tilesize, 2 * size);
        Fill.square(x * tilesize, (y - 1) * tilesize, 2 * size);
        Fill.square(x * tilesize, (y + 1) * tilesize, 2 * size);
    }

}
