package tjTool.world.graphics;

import arc.graphics.Mesh;
import arc.graphics.Texture;
import arc.graphics.VertexAttribute;

import static tjTool.TheJourney.theJourney;
import static tjTool.core.TjFunc.*;

public class MultiModel {
    public static void load() {
        if (vertices == null) return;
        ring = new Texture(pixmap(theJourney.root.child("texture").child("anvil-ring.tj")));
        final int r = 4;
        setVertices = new float[vertices.length * r];
        setIndices = new short[setVertices.length / 5 / 4 * 6];
        final int area = vertices.length / 20;
        forRange(r, dir -> forRange(area, idx -> {
            forRange(4, i -> {
                int c = idx * 20 + i * 5;
                int cc = vertices.length * dir + c;
                setVertices[cc] = vertices[c] * multi;
                setVertices[cc + 1] = vertices[c + 1] * multi;
                setVertices[cc + 2] = vertices[c + 2] * multi;
                setVertices[cc + 3] = vertices[c + 3] / (float) ring.width;
                setVertices[cc + 4] = vertices[c + 4] / (float) ring.height;
                if (dir > 0) {
                    setVertices[cc] = -setVertices[cc + 1 - vertices.length];
                    setVertices[cc + 1] = setVertices[cc - vertices.length];
                }
            });
            int i = (area * dir + idx) * 6;
            int v = (area * dir + idx) * 4;
            setIndices[i] = (short) v;
            setIndices[i + 1] = (short) (v + 1);
            setIndices[i + 2] = (short) (v + 2);
            setIndices[i + 3] = (short) v;
            setIndices[i + 4] = (short) (v + 2);
            setIndices[i + 5] = (short) (v + 3);
        }));
        ringMesh = new Mesh(true, setVertices.length / 5, setIndices.length,
                VertexAttribute.position3,
                VertexAttribute.texCoords
        ).setVertices(setVertices).setIndices(setIndices);
        vertices = null;
    }

    public static Texture ring;
    public static float multi = 0.01f;
    public static Mesh ringMesh;
    public static float[] setVertices;
    public static short[] setIndices;
    public static int[] vertices = new int[]{
            -32, +92, + 8,   60,  0,
            -32, +68, + 8,   60, 24,
            +32, +68, + 8,  124, 24,
            +32, +92, + 8,  124,  0,

            +32, +92, - 8,  124,  0,
            +32, +68, - 8,  124, 24,
            -32, +68, - 8,   60, 24,
            -32, +92, - 8,   60,  0,

            -32, +92, + 8,   60,  0,
            +32, +92, + 8,  124,  0,
            +32, +92, - 8,  124,  1,
            -32, +92, - 8,   60,  1,

            -32, +68, - 8,   60, 24,
            +32, +68, - 8,  124, 24,
            +32, +68, + 8,  124, 23,
            -32, +68, + 8,   60, 23,

            -32, +92, + 8,   60,  0,
            -32, +92, - 8,   61,  0,
            -32, +68, - 8,   61, 24,
            -32, +68, + 8,   60, 24,

            +32, +68, + 8,   60, 24,
            +32, +68, - 8,   61, 24,
            +32, +92, - 8,   61,  0,
            +32, +92, + 8,   60,  0,


            -56, +92, + 4,   36,  0,
            -56, +76, + 4,   36, 16,
            -32, +76, + 4,   60, 16,
            -32, +92, + 4,   60,  0,

            -32, +92, - 4,   60,  0,
            -32, +76, - 4,   60, 16,
            -56, +76, - 4,   36, 16,
            -56, +92, - 4,   36,  0,

            -56, +92, + 4,   36,  0,
            -32, +92, + 4,   60,  0,
            -32, +92, - 4,   60,  1,
            -56, +92, - 4,   36,  1,

            -56, +76, - 4,   36, 16,
            -32, +76, - 4,   60, 16,
            -32, +76, + 4,   60, 15,
            -56, +76, + 4,   36, 15,


            +32, +92, + 4,   60,  0,
            +32, +76, + 4,   60, 16,
            +56, +76, + 4,   36, 16,
            +56, +92, + 4,   36,  0,

            +56, +92, - 4,   36,  0,
            +56, +76, - 4,   36, 16,
            +32, +76, - 4,   60, 16,
            +32, +92, - 4,   60,  0,

            +56, +92, - 4,   36,  1,
            +32, +92, - 4,   60,  1,
            +32, +92, + 4,   60,  0,
            +56, +92, + 4,   36,  0,

            +56, +76, + 4,   36, 15,
            +32, +76, + 4,   60, 15,
            +32, +76, - 4,   60, 16,
            +56, +76, - 4,   36, 16,


            -64, +92, +12,   28,  0,
            -64, +64, +12,   28, 28,
            -56, +56, +12,   36, 36,
            -56, +92, +12,   36,  0,
            -92, +64, +12,    0, 28,
            -92, +56, +12,    0, 36,
            -56, +56, +12,   36, 36,
            -64, +64, +12,   28, 28,

            -56, +92, -12,   36,  0,
            -56, +56, -12,   36, 36,
            -64, +64, -12,   28, 28,
            -64, +92, -12,   28,  0,
            -64, +64, -12,   28, 28,
            -56, +56, -12,   36, 36,
            -92, +56, -12,    0, 36,
            -92, +64, -12,    0, 28,

            -64, +92, +12,   28,  0,
            -56, +92, +12,   36,  0,
            -56, +92, -12,   36,  1,
            -64, +92, -12,   28,  1,

            -92, +64, -12,   28,  1,
            -92, +56, -12,   36,  1,
            -92, +56, +12,   36,  0,
            -92, +64, +12,   28,  0,

            -56, +92, +12,   35,  0,
            -56, +56, +12,   35, 36,
            -56, +56, -12,   36, 36,
            -56, +92, -12,   36,  0,

            -92, +56, -12,   36,  0,
            -56, +56, -12,   36, 36,
            -56, +56, +12,   35, 36,
            -92, +56, +12,   35,  0,

            -64, +92, +12,   28,  0,
            -64, +92, -12,   29,  0,
            -64, +64, -12,   29, 28,
            -64, +64, +12,   28, 28,

            -64, +64, +12,   28, 28,
            -64, +64, -12,   29, 28,
            -92, +64, -12,   29,  0,
            -92, +64, +12,   28,  0,


            -88, +88, + 8,    4,  4,
            -88, +64, + 8,    4, 28,
            -64, +64, + 8,   28, 28,
            -64, +88, + 8,   28,  4,

            -64, +88, - 8,   28,  4,
            -64, +64, - 8,   28, 28,
            -88, +64, - 8,    4, 28,
            -88, +88, - 8,    4,  4,

            -88, +88, + 8,    4,  4,
            -64, +88, + 8,   28,  4,
            -64, +88, - 8,   28,  5,
            -88, +88, - 8,    4,  5,

            -88, +88, - 8,    4,  5,
            -88, +64, - 8,   28,  5,
            -88, +64, + 8,   28,  4,
            -88, +88, + 8,    4,  4,
    };
}
