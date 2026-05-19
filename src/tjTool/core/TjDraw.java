package tjTool.core;

import arc.func.*;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.gen.Building;
import mindustry.graphics.Pal;
import mindustry.world.Block;

import static arc.math.geom.Geometry.*;
import static mindustry.Vars.tilesize;

@SuppressWarnings("unused")
public class TjDraw {

    public static String colorToString(Color color) {
        return "[#" + color + "]";
    }

    public static Color rainbow() {
        return rainbow(25f, 100f);
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

    public static void drawPlace(Block block, int x, int y, boolean valid) {
        float width = 2;
        float[] r = new float[]{block.size * tilesize / 2f, 0, tilesize * 16};
        float[] wr = new float[]{width / 2f, block.size * tilesize / 2f - width};
        float cx = x * tilesize + block.offset;
        float cy = y * tilesize + block.offset;
        Color color = valid ? rainbow() : Pal.remove.cpy();
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
        Color color = rainbow();
        float from = color.a(0.25f).toFloatBits();
        float to = color.a(0).toFloatBits();
        for (int i = 0; i < 4; ++i)
            Fill.quad(
                    cx + r[0] * d8edge(i).x, cy + r[0] * d8edge(i).y, from,
                    cx + r[0] * d8edge(i - 1).x, cy + r[0] * d8edge(i - 1).y, from,
                    cx + r[(i + 1) % 2] * d8edge(i - 1).x, cy + r[i % 2] * d8edge(i - 1).y, to,
                    cx + r[(i + 1) % 2] * d8edge(i).x, cy + r[i % 2] * d8edge(i).y, to
            );
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
