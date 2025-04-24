package a3.Client;

import org.joml.*;
import tage.*;
import tage.shapes.ManualObject;
import static java.lang.Math.*;

public class Pyramid extends ManualObject {
    public Pyramid() { 
        super(); 
        createPyramid();
    }

    private void createPyramid() {
        float[] vertices = new float[] {
            // Front face
            0.0f,  1.0f,  0.0f,  // Top
            -1.0f, -1.0f,  1.0f,  // Bottom left
             1.0f, -1.0f,  1.0f,  // Bottom right
            
            // Right face
             0.0f,  1.0f,  0.0f,  // Top
             1.0f, -1.0f,  1.0f,  // Bottom front
             1.0f, -1.0f, -1.0f,  // Bottom back
            
            // Back face
             0.0f,  1.0f,  0.0f,  // Top
             1.0f, -1.0f, -1.0f,  // Bottom right
            -1.0f, -1.0f, -1.0f,  // Bottom left
            
            // Left face
             0.0f,  1.0f,  0.0f,  // Top
            -1.0f, -1.0f, -1.0f,  // Bottom back
            -1.0f, -1.0f,  1.0f,  // Bottom front

            // Bottom face (split into 2 triangles)
            -1.0f, -1.0f,  1.0f,  // Front left
             1.0f, -1.0f,  1.0f,  // Front right
            -1.0f, -1.0f, -1.0f,  // Back left

             1.0f, -1.0f,  1.0f,  // Front right
             1.0f, -1.0f, -1.0f,  // Back right
            -1.0f, -1.0f, -1.0f   // Back left
        };

        float[] texCoords = new float[] {
            0.5f, 1.0f,  0.0f, 0.0f,  1.0f, 0.0f, // Front
            0.5f, 1.0f,  0.0f, 0.0f,  1.0f, 0.0f, // Right
            0.5f, 1.0f,  0.0f, 0.0f,  1.0f, 0.0f, // Back
            0.5f, 1.0f,  0.0f, 0.0f,  1.0f, 0.0f, // Left
            0.0f, 1.0f,  1.0f, 1.0f,  0.0f, 0.0f, // Bottom tri 1
            1.0f, 1.0f,  1.0f, 0.0f,  0.0f, 0.0f  // Bottom tri 2
        };
        
        float[] normals = new float[] {
            0.0f,  0.5f,  1.0f,  0.0f,  0.5f,  1.0f,  0.0f,  0.5f,  1.0f, // Front
            1.0f,  0.5f,  0.0f,  1.0f,  0.5f,  0.0f,  1.0f,  0.5f,  0.0f, // Right
            0.0f,  0.5f, -1.0f,  0.0f,  0.5f, -1.0f,  0.0f,  0.5f, -1.0f, // Back
           -1.0f,  0.5f,  0.0f, -1.0f,  0.5f,  0.0f, -1.0f,  0.5f,  0.0f, // Left
            0.0f, -1.0f,  0.0f,  0.0f, -1.0f,  0.0f,  0.0f, -1.0f,  0.0f, // Bottom
            0.0f, -1.0f,  0.0f,  0.0f, -1.0f,  0.0f,  0.0f, -1.0f,  0.0f
        };

        setNumVertices(vertices.length / 3);
        setVertices(vertices);
        setTexCoords(texCoords);
        setNormals(normals);
        setWindingOrderCCW(false);
    }
}
