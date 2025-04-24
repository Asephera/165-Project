package a3.Client;

import tage.*;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;

public class Attach extends AbstractInputAction
{   private MyGame game;
    private static GameObject object;
    private Car car;

    public Attach(MyGame g, GameObject obj) 
    {   game = g;
        object = obj;
        car = game.getCar();
    }

    public static void updateObject(GameObject obj) {
        object = obj;
    }

    private void route() {
    }

    @Override
    public void performAction(float time, Event e) 
    {   System.out.println("Attaching!");
        // +++++ code for attaching lol
        switch(object.getType()) {
            case "fLDoor":
                car.setFLDoor(object);
                break;
            case "fRDoor":
                car.setFRDoor(object);
                break;
            case "bLDoor":
                car.setBLDoor(object);    
                break;
            case "bRDoor":
                car.setBRDoor(object);
                break;
            case "fLTire":
                car.setFLTire(object);
                break;
            case "fRTire":
                car.setFRTire(object);
                break;
            case "bLTire":
                car.setBLTire(object);
                break;
            case "bRTire":
                car.setBRTire(object);
                break;
            case "engine":
                car.setEngine(object);
                break;
            case "spoiler":
                car.setSpoiler(object);
                break;
        }
    }
}
