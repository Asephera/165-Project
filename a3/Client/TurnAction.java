package a3.Client;

import tage.*;
import tage.physics.*;
import java.lang.Math;
import org.joml.*;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;

public class TurnAction extends AbstractInputAction
{   private MyGame game;
    private float speed;
    private static float modifier = 1;
    private GameObject av;
    private PhysicsObject car;
    Vector3f rightVec, fwdVec;
    private ProtocolClient protClient;

    public TurnAction(MyGame g, float spd, ProtocolClient p) 
    {   game = g;
        speed = spd;
        protClient = p;
    }

    public static void updateModifier(double mod) {
        modifier = (float)mod;
    }

    @Override
    public void performAction(float time, Event e) 
    {   float keyValue = e.getValue();
        if (Math.abs(keyValue) < 0.2) return; // deadzone

        av = game.getAvatar();
        car = game.getPhysCar();
        rightVec = av.getWorldRightVector().mul(-speed * keyValue * modifier * 10);
        fwdVec = av.getWorldForwardVector().mul(3);
        car.applyForce(rightVec.x, rightVec.y, rightVec.z, fwdVec.x, fwdVec.y, fwdVec.z);

        protClient.sendYawMessage(speed * av.getLocalRotation().m11()); //I THINK this gets the rotation around the y axis from the rotation matrix
        // it is, BUT you should just send the whole ass rotation matrix or something and SET the rotation of the ghost avatar, and not trying to turn it in accordance with PHYSICS.

        //av.globalYaw(speed * keyValue * modifier);
    }
}
