package tjTool.core;

import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.gen.Building;

import static mindustry.Vars.tilesize;

public class TjDraw {

    public static String toString(Color color) {
        return "[#" + color + "]";
    }

    public static Color rainbow() {
        return rainbow(25f, 100f);
    }

    public static Color rainbow(float s, float v) {
        return Color.HSVtoRGB(Time.time % 360, s, v);
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
                    building.x + Geometry.d4(i).x * r + Geometry.d4(i).y * w,
                    building.y + Geometry.d4(i).y * r - Geometry.d4(i).x * w);
            if(f < 0.5f) Lines.linePoint(
                    building.x + Geometry.d4(i).x * r - Geometry.d4(i).y * w,
                    building.y + Geometry.d4(i).y * r + Geometry.d4(i).x * w);
        }
        Lines.endLine(true);

        Draw.reset();
    }

}
