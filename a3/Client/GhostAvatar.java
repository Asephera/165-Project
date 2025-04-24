package a3.Client;

import java.util.UUID;

import tage.*;
import org.joml.*;

public class GhostAvatar extends GameObject {
    UUID uuid;

    public GhostAvatar(UUID id, ObjShape s, TextureImage t, Vector3f p) {
        super(GameObject.root(), s, t);
        uuid = id;
        setPosition(p);
    }

    public UUID getID() { return uuid; }
    public void setPosition(Vector3f m) { setLocalLocation(m); }
    public Vector3f getPosition() { return getWorldLocation(); }
}
