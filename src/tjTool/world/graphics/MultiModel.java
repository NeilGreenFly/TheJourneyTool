package tjTool.world.graphics;

import arc.graphics.Mesh;
import arc.graphics.Texture;
import arc.graphics.VertexAttribute;
import arc.util.Tmp;

import static tjTool.TheJourney.theJourney;
import static tjTool.core.TjFunc.*;

public class MultiModel {
    public static void load() {
        if (vertices == null) return;
        ring = new Texture(pixmap(theJourney.root.child("texture").child("anvil-ring.tj")));
        final int r = 4;
        final int verSize = 8; // position3 + texCoords + normal
        final int areaSize = verSize * 4;
        final int areaCount = vertices.length / 20;
        final int singleSize = areaSize * areaCount;
        setVertices = new float[singleSize * r];
        setIndices = new short[setVertices.length / areaSize * 6];
        forRange(r, dir -> forRange(areaCount, areaIndex -> {
            int baseIndex = singleSize * dir + areaSize * areaIndex;
            forRange(4, i -> {
                int source = 20 * areaIndex + 5 * i;
                int c = baseIndex + verSize * i;
                setVertices[c] = vertices[source] * multi;
                setVertices[c + 1] = vertices[source + 1] * multi;
                setVertices[c + 2] = vertices[source + 2] * multi;
                setVertices[c + 3] = vertices[source + 3] / (float) ring.width;
                setVertices[c + 4] = vertices[source + 4] / (float) ring.height;
                if (dir > 0) {
                    setVertices[c] = -setVertices[c + 1 - singleSize];
                    setVertices[c + 1] = setVertices[c - singleSize];
                }
            });
            var nor = Tmp.v31.set(
                    setVertices[baseIndex + verSize] - setVertices[baseIndex],
                    setVertices[baseIndex + verSize + 1] - setVertices[baseIndex + 1],
                    setVertices[baseIndex + verSize + 2] - setVertices[baseIndex + 2]
            ).crs(
                    setVertices[baseIndex + verSize * 2] - setVertices[baseIndex + verSize],
                    setVertices[baseIndex + verSize * 2 + 1] - setVertices[baseIndex + verSize + 1],
                    setVertices[baseIndex + verSize * 2 + 2] - setVertices[baseIndex + verSize + 2]
            ).nor();
            forRange(4, i -> {
                int c = baseIndex + verSize * i;
                setVertices[c + 5] = nor.x;
                setVertices[c + 6] = nor.y;
                setVertices[c + 7] = nor.z;
            });
            int i = (areaCount * dir + areaIndex) * 6;
            int v = (areaCount * dir + areaIndex) * 4;
            setIndices[i] = (short) v;
            setIndices[i + 1] = (short) (v + 1);
            setIndices[i + 2] = (short) (v + 2);
            setIndices[i + 3] = (short) v;
            setIndices[i + 4] = (short) (v + 2);
            setIndices[i + 5] = (short) (v + 3);
        }));
        ringMesh = new Mesh(true, setVertices.length / verSize, setIndices.length,
                VertexAttribute.position3,
                VertexAttribute.texCoords,
                VertexAttribute.normal
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
