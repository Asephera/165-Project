package a3.Client;

import tage.*;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;

public class Sprint extends AbstractInputAction
{   private MyGame game;
    private float sprint;

    public Sprint(MyGame g, float sprt) 
    {   game = g;
        sprint = sprt;
    }

    @Override
    public void performAction(float time, Event sprt) 
    {   System.out.println("Sprinting");
        game.setSprint(sprint);
    }
    
}