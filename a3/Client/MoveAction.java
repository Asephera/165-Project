package a3.Client;

import tage.*;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;

public class MoveAction extends AbstractInputAction
{   private MyGame game;
    private float speed;
    private static float modifier = 1;
    private GameObject av;
    private ProtocolClient protClient;

    public MoveAction(MyGame g, float spd, ProtocolClient p) 
    {   game = g;
        speed = spd; //default speed
        protClient = p;
    }

    public static void updateModifier(double mod) {
        modifier = (float)mod;
    }

    @Override
    public void performAction(float time, Event e) 
    {   float keyValue = e.getValue();
        if (keyValue > -0.2 && keyValue < 0.2) return; // deadzone
        
        float finalSpeed = speed * keyValue * modifier;
        av = game.getAvatar();
        if(game.avatarBounding(finalSpeed)) { av.move(finalSpeed); }
        protClient.sendMoveMessage(av.getWorldLocation());
    }   
}
