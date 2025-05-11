package a3.Client;

import tage.GameObject;
import tage.ObjShape;
import tage.TextureImage;
import org.joml.Vector3f;

public class GhostNPC extends GameObject {
    private int id;

    public GhostNPC(int id, ObjShape s, TextureImage t, Vector3f p) {
        super(GameObject.root(), s, t);
        this.id = id;
        setPosition(p);
    }

    public int getID() { return id; }
    public void setPosition(Vector3f m) { setLocalLocation(m); }
    public Vector3f getPosition() { return getWorldLocation(); }

}
