package a3.Client;

import tage.*;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;

public class Disarm extends AbstractInputAction
{   private MyGame game;
    private static GameObject object;

    public Disarm(MyGame g, GameObject obj) 
    {   game = g;
        object = obj;
    }

    public static void updateObject(GameObject obj) {
        object = obj;
    }

    @Override
    public void performAction(float time, Event e) 
    {   System.out.println("Disarming!");
        game.disarm(object);
    }
    
}
