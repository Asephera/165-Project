package a3.Client;

import tage.GameObject;
import org.joml.*;

public class Car extends GameObject{
    private Vector3f engineLoc = new Vector3f(0.0f,0.0f,0.0f), 
                     fLDoorLoc = new Vector3f(0.0f,0.0f,0.0f), 
                     fRDoorLoc = new Vector3f(0.0f,0.0f,0.0f), 
                     bLDoorLoc = new Vector3f(0.0f,0.0f,0.0f), 
                     bRDoorLoc = new Vector3f(0.0f,0.0f,0.0f), 
                     spoilerLoc = new Vector3f(0.0f,0.0f,0.0f), 
                     fLTireLoc = new Vector3f(0.0f,0.0f,0.0f), 
                     fRTireLoc = new Vector3f(0.0f,0.0f,0.0f), 
                     bLTireLoc = new Vector3f(0.0f,0.0f,0.0f), 
                     bRTireLoc = new Vector3f(0.0f,0.0f,0.0f);
    private GameObject engine, fLDoor, fRDoor, bLDoor, bRDoor, spoiler, fLTire, fRTire, bLTire, bRTire;

    public Car() {
        super();
        //instantiateParts();
    }

    // maybe use to pre-set the locations of object parts
    private void instantiateParts() {

    }

    // feelin cute might delete later
    private void set(GameObject heldPart, GameObject carPart, Vector3f location) {
        if(carPart != null) {
            // error popup
            return;
        }
        
        heldPart.setLocalLocation(this.getWorldLocation().add(location));
        heldPart.setParent(this);
        heldPart.propagateRotation(true); // + potential for abstracting the propogation settings to call for every time an object is attached
        carPart = heldPart;
    }

    // removes car part and puts it into player hand
    private GameObject remove(GameObject player, GameObject carPart) {
        if(carPart == null) {
            // error popup
            return null;
        }
        if(player.hasChildren()) {
            // error popup
            return null;
        }

        GameObject obj = carPart; // copy door to external
        carPart = null; // delete door
        obj.setParent(player); // unlink external obj
        obj.setLocalLocation(player.getWorldLocation().add(new Vector3f(0.0f, -0.2f, 0.2f))); 
        // ++++ might not work, look into how to get player's hand location
        // backup implementation: obj.setLocalLocation(obj.getWorldLocation().add(this.getWorldRightVector())); // move external object away from car
        return obj;
    }

    // Car Part Setters
    // Doors
    public void setFLDoor(GameObject obj) { set(obj, fLDoor, fLDoorLoc); }
    public void setFRDoor(GameObject obj) { set(obj, fRDoor, fRDoorLoc); }
    public void setBLDoor(GameObject obj) { set(obj, bLDoor, bLDoorLoc); }
    public void setBRDoor(GameObject obj) { set(obj, bRDoor, bRDoorLoc); }
    // Tires
    public void setFLTire(GameObject obj) { set(obj, fLTire, fLTireLoc); }
    public void setFRTire(GameObject obj) { set(obj, fRTire, fRTireLoc); }
    public void setBLTire(GameObject obj) { set(obj, bLTire, bLTireLoc); }
    public void setBRTire(GameObject obj) { set(obj, bRTire, bRTireLoc); }
    // Misc
    public void setEngine(GameObject obj) { set(obj, engine, engineLoc); }
    public void setSpoiler(GameObject obj) { set(obj, spoiler, spoilerLoc); }

    // Car Part Removers
    // Doors
    public GameObject removeFLDoor(GameObject player) { return remove(player, fLDoor); }
    public GameObject removeFRDoor(GameObject player) { return remove(player, fRDoor); }
    public GameObject removeBLDoor(GameObject player) { return remove(player, bLDoor); }
    public GameObject removeBRDoor(GameObject player) { return remove(player, bRDoor); }
    // Tires
    public GameObject removeFLTire(GameObject player) { return remove(player, fLTire); }
    public GameObject removeFRTire(GameObject player) { return remove(player, fRTire); }
    public GameObject removeBLTire(GameObject player) { return remove(player, bLTire); }
    public GameObject removeBRTire(GameObject player) { return remove(player, bRTire); }
    // Misc
    public GameObject removeEngine(GameObject player) { return remove(player, engine); }
    public GameObject removeSpoiler(GameObject player) { return remove(player, spoiler); }

    // Car Part Getters
    // Doors
    public GameObject getflDoor() { return fLDoor; }
    public GameObject getfrDoor() { return fRDoor; }
    public GameObject getblDoor() { return bLDoor; }
    public GameObject getbrDoor() { return bRDoor; }
    // Tires
    public GameObject getflTire() { return fLTire; }
    public GameObject getfrTire() { return fRTire; }
    public GameObject getblTire() { return bLTire; }
    public GameObject getbrTire() { return bRTire; }
    // Misc
    public GameObject getEngine() { return engine; }
    public GameObject getSpoiler() { return spoiler; }
}
