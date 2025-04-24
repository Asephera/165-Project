package a3.Client;

import tage.shapes.ManualObject;

public class Plus extends ManualObject{
    private float[] vertices, texCoords, normals;

    public Plus() {
		super();
        createPlus();
    }

    private void createPlus() {
        // quick type vars
		float b = 0.25f;
		float a = 1.0f;

		// Verticies - I wrote these manually (woe is me)
		vertices = new float[]
		{	-a,  b, -b, -a, -b, -b, -a, -b,  b,     -a,  b, -b, -a, -b,  b, -a,  b,  b, //left left
			-a,  b, -b, -a,  b,  b, -b,  b,  b,     -a,  b, -b, -b,  b,  b, -b,  b, -b, //left top
			-a, -b, -b, -a, -b,  b, -b, -b,  b,     -a, -b, -b, -b, -b,  b, -b, -b, -b, //left  bottom
			-a, -b,  b, -b, -b,  b, -a,  b,  b,     -a,  b,  b, -b, -b,  b, -b,  b,  b, //left front
			-a, -b, -b, -b, -b, -b, -a,  b, -b,     -a,  b, -b, -b, -b, -b, -b,  b, -b, //left  back

			a,  b, -b,  a, -b, -b,  a, -b,  b,       a,  b, -b,  a, -b,  b,  a,  b,  b, //right right
			a,  b, -b,  a,  b,  b,  b,  b,  b,       a,  b, -b,  b,  b,  b,  b,  b, -b, //right top
			a, -b, -b,  a, -b,  b,  b, -b,  b,       a, -b, -b,  b, -b,  b,  b, -b, -b, //right top
			a, -b,  b,  b, -b,  b,  a,  b,  b,       a,  b,  b,  b, -b,  b,  b,  b,  b, //right front
			a, -b, -b,  b, -b, -b,  a,  b, -b,       a,  b, -b,  b, -b, -b,  b,  b, -b, //right  back

			-b,  a, -b, -b,  a,  b,  b,  a, -b,      b,  a, -b, -b,  a,  b,  b,  a,  b, //top top
			-b,  a, -b, -b,  b, -b, -b,  b,  b,     -b,  b,  b, -b,  a, -b, -b,  a,  b, //top left
			 b,  a, -b,  b,  b, -b,  b,  b,  b,      b,  b,  b,  b,  a, -b,  b,  a,  b, //top right

			-b, -a, -b, -b, -a,  b,  b, -a, -b,      b, -a, -b, -b, -a,  b,  b, -a,  b, //bottom  bottom
			-b, -a, -b, -b, -b, -b, -b, -b,  b,     -b, -b,  b, -b, -a, -b, -b, -a,  b, //bottom left
			 b, -a, -b,  b, -b, -b,  b, -b,  b,      b, -b,  b,  b, -a, -b,  b, -a,  b, //bottom left

			-b,  a,  b, -b, -a,  b,  b, -a,  b,     -b,  a,  b,  b, -a,  b,  b,  a,  b, //front face
			-b,  a, -b, -b, -a, -b,  b, -a, -b,     -b,  a, -b,  b, -a, -b,  b,  a, -b  //back face
			// 108 total verts
		};

        // Texture coordinates - procedural is so nice
		texCoords = new float[108 * 2];
		for (int i = 0; i < 108; i += 6) {
			texCoords[i * 2 + 0] = 0.0f; texCoords[i * 2 + 1] = 0.0f;
			texCoords[i * 2 + 2] = 1.0f; texCoords[i * 2 + 3] = 0.0f;
			texCoords[i * 2 + 4] = 1.0f; texCoords[i * 2 + 5] = 1.0f;
			texCoords[i * 2 + 6] = 0.0f; texCoords[i * 2 + 7] = 0.0f;
			texCoords[i * 2 + 8] = 1.0f; texCoords[i * 2 + 9] = 1.0f;
			texCoords[i * 2 + 10] = 0.0f; texCoords[i * 2 + 11] = 1.0f;
		}
		setNumVertices(vertices.length / 3);
		setVertices(vertices);
		setTexCoords(texCoords);
		setNormals(normals);
		setWindingOrderCCW(false);
	}

    public int getNumVertices() { return (vertices.length / 3); }
    public float[] getVertices() { return vertices; }
    public float[] getTexCoords() { return texCoords; }
    public float[] getNormals() { return normals; }
}
