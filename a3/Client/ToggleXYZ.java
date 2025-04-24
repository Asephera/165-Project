package a3.Client;

import tage.*;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;

public class ToggleXYZ extends AbstractInputAction
{   private MyGame game;
    private static GameObject xLine;
    private static GameObject yLine;
    private static GameObject zLine;
    private static boolean toggle = true;

    public ToggleXYZ(MyGame g, GameObject obj1, GameObject obj2 ,GameObject obj3) 
    {   game = g;
        xLine = obj1;
        yLine = obj2;
        zLine = obj3;
    }

    @Override
    public void performAction(float time, Event e) 
    {   System.out.println("Toggling XYZ!");
        if(toggle) { 
            xLine.getRenderStates().disableRendering();
            yLine.getRenderStates().disableRendering(); 
            zLine.getRenderStates().disableRendering(); 
        }
        else { 
            xLine.getRenderStates().enableRendering(); 
            yLine.getRenderStates().enableRendering(); 
            zLine.getRenderStates().enableRendering(); 
        }
        toggle = !toggle;
    }
    
}
