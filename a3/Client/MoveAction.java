package a3.Client;

import tage.*;
import tage.physics.*;
import tage.shapes.AnimatedShape;

import org.joml.*;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;

public class MoveAction extends AbstractInputAction
{   private MyGame game;
    private float speed;
    private static float modifier = 1;
    private GameObject av;
    private PhysicsObject car;
    private AnimatedShape carS;
    Vector3f fwdVec;
    private ProtocolClient protClient;
    private Camera cam;

    public MoveAction(MyGame g, float spd, ProtocolClient p) 
    {   game = g;
        speed = spd; //default speed
        protClient = p;
        cam = game.getEngine().getRenderSystem().getViewport("MAIN").getCamera();
    }

    public static void updateModifier(double mod) {
        modifier = (float)mod;
    }

    @Override
    public void performAction(float time, Event e) 
    {   float keyValue = e.getValue();
        if (keyValue > -0.2 && keyValue < 0.2) return; // deadzone
        float finalSpeed = speed * keyValue * modifier;
        
        if(game.getViewType() == "car") {
            // simple car sliding movement
            av = game.getAvatar();
            car = game.getPhysCar();
            carS = game.getTires();

            fwdVec = av.getWorldForwardVector().mul(finalSpeed * 3000);
            car.applyForce(fwdVec.x, fwdVec.y, fwdVec.z, 0, 0, 0);

            protClient.sendMoveMessage(av.getWorldLocation());
        }
        else {
            cam.move(finalSpeed);
        }
        

        // old poop NON physics movement, pfff what a looser (I am dying inside)
        //if(game.avatarBounding(finalSpeed)) { av.move(finalSpeed); }
    }   
}
