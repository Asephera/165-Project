package a3.Client;

import tage.*;
import java.lang.Math;
import org.joml.*;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;

public class TurnAction extends AbstractInputAction
{   private MyGame game;
    private float speed;
    private static float modifier = 1;
    private GameObject av;

    public TurnAction(MyGame g, float spd) 
    {   game = g;
        speed = spd;
    }

    public static void updateModifier(double mod) {
        modifier = (float)mod;
    }

    @Override
    public void performAction(float time, Event e) 
    {   float keyValue = e.getValue();
        if (Math.abs(keyValue) < 0.2) return; // deadzone

        av = game.getAvatar();
        av.globalYaw(speed * keyValue * modifier);
    }
}
