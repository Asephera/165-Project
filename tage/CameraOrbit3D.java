package tage;

import java.lang.Math;
import org.joml.*;

import tage.input.InputManager;
import net.java.games.input.Event;
import tage.input.action.AbstractInputAction;

/**
* CameraOrbit3D specifies parameters for handling camera viewports
* It handles inputs to azimuth, elevate, and radius orbit cameras
* It handles inputs to pan and zoom overhead cameras
* <p>
* This is a camera controller with specific intentions.
* The default orbit camera is positioned behind and above, looking at a specified game object.
* The default overhead camera position is at (0,15,0) looking down the -Y axis towards the origin.
* @author Connor Yep
*/

public class CameraOrbit3D {
    private Engine engine;
    private Camera camera;
    private GameObject avatar;
    private float cameraAzimuth;
    private float cameraElevation;
    private float cameraRadius;
    private float newY;
    private boolean isOrbit;
    private String gp;
    Vector3f camPos;

    /** Instantiates the camera controller for the specified cam at respective locations depending on if isOrbit is true or false */
    public CameraOrbit3D(Camera cam, GameObject av, Engine e, boolean orbit) {
        engine = e;
        camera = cam;
        avatar = av;
        isOrbit = orbit;
        cameraAzimuth = 0.0f;
        
        if(isOrbit) {
            cameraElevation = 30.0f;
            cameraRadius = 15.0f;
        }
        else { // (isOverhead)
            cameraElevation = 90.0f;
            cameraRadius = 15.0f;
        }
        
        setupInputs();
        updateCameraPosition();
    }

    /** instantiates actions and input manager, then assigns inputs to respective actions */
    private void setupInputs() {
        InputManager im = engine.getInputManager();
        gp = im.getFirstGamepadName();
        
        if(isOrbit) {
            OrbitAzimuthAction azmLeft = new OrbitAzimuthAction(1f); 
            OrbitAzimuthAction azmRight = new OrbitAzimuthAction(-1f);
            OrbitRadiusAction radIn = new OrbitRadiusAction(1f);
            OrbitRadiusAction radOut = new OrbitRadiusAction(-1f);
            OrbitElevationAction eleUp = new OrbitElevationAction(1f);
            OrbitElevationAction eleDown = new OrbitElevationAction(-1f);
            if(gp != null) {
                System.out.println("gamepad detected!");
                im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Axis.Z, azmLeft, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    
                im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Button.A, radOut, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Button.B, radIn, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    
                im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Axis.RZ, eleDown, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            }
            im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.J, azmRight, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.L, azmLeft, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        
            im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.O, radOut, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.U, radIn, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    
            im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.I, eleUp, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.K, eleDown, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        }
        else {
            PanCam panUp = new PanCam(0, -1, 0.1f);
            PanCam panDown = new PanCam(0, 1, 0.1f);
            PanCam panLeft = new PanCam(-1, 0, 0.1f);
            PanCam panRight = new PanCam(1, 0, 0.1f);
            zoomOCam zoomIn = new zoomOCam(1);
            zoomOCam zoomOut = new zoomOCam(-1);

            im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.NUMPAD8, panUp, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.NUMPAD5, panDown, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.NUMPAD4, panLeft, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.NUMPAD6, panRight, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.NUMPAD7, zoomIn, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.NUMPAD9, zoomOut, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        }
    }

    /** updates camera position depending on if isOrbit is true or false */
    public void updateCameraPosition() {
        if (isOrbit) {
            Vector3f avatarRot = avatar.getWorldForwardVector();
            double avatarAngle = Math.toDegrees((double)avatarRot.angleSigned(new Vector3f(0,0,-1), new Vector3f(0,1,0)));
            float totalAz = cameraAzimuth - (float)avatarAngle;
            double theta = Math.toRadians(totalAz);
            double phi = Math.toRadians(cameraElevation);
            float x = cameraRadius * (float)(Math.cos(phi) * Math.sin(theta));
            float y = cameraRadius * (float)(Math.sin(phi));
            float z = cameraRadius * (float)(Math.cos(phi) * Math.cos(theta));
            camera.setLocation(new Vector3f(x,y,z).add(avatar.getWorldLocation()));
            camera.lookAt(avatar);
        }
        else { //if overhead cam
            camPos = camera.getLocation();
            camera.lookAt(new Vector3f(camPos.x, 0, camPos.z)); // Always looking down
        }
    }

    /** Action class for panning overhead cameras */
    private class PanCam extends AbstractInputAction 
    {
        private float panX, panZ;
        private float speed;
        PanCam(float px, float pz, float spd) {
            panX = px;
            panZ = pz;
            speed = spd;
        }

        public void performAction(float time, Event event) {
            if(isOrbit) { return; }
            Vector3f camPos = camera.getLocation();
            camera.setLocation(camPos.add(panX * speed, 0, panZ * speed));
            updateCameraPosition();
        }
    }

    /** Action class for zooming overhead cameras along the y axis */
    private class zoomOCam extends AbstractInputAction 
    {
        private float speed;
        float camZoom;
        zoomOCam(float spd) {
            speed = spd;
        }

        public void performAction(float time, Event event) {
            if(isOrbit) { return; }

            // handle gamepad variable inputs
            float key = event.getValue() * speed;
            if (key < -0.2)
            {   camZoom=-0.2f; }
            else
            {   if (key > 0.2)
                {   camZoom=0.2f; }
                else
                {   return; }
            }

            System.out.println("zooming overhead..");
            newY = camZoom + camera.getLocation().y;
            newY = Math.max(5, Math.min(newY, 50));
            camera.setLocation(new Vector3f(camPos.x, newY, camPos.z));
            updateCameraPosition();
        }
    }

    /** Action class for azimuth rotation of orbit cameras */
    private class OrbitAzimuthAction extends AbstractInputAction
    {   
        private float speed;
        OrbitAzimuthAction(float spd) {
            speed = spd;
            //super();
        }
        

        public void performAction(float time, Event event)
        {   float rotAmount;
            float key = event.getValue() * speed;
            if (key < -0.2)
            {   rotAmount=-0.2f; }
            else
            {   if (key > 0.2)
                {   rotAmount=0.2f; }
                else
                {   return; }
            }

            System.out.println("orbiting azimuth..");
            cameraAzimuth += rotAmount * 4;
            cameraAzimuth = cameraAzimuth % 360;
            updateCameraPosition();
        } 
    }

    /** Action class for elevation rotation of orbit cameras */
    private class OrbitElevationAction extends AbstractInputAction
    {   
        private float speed;
        OrbitElevationAction(float spd) {
            speed = spd;
            //super();
        }
        

        public void performAction(float time, Event event)
        {   float elevAmount;
            float key = event.getValue() * speed;
            if (key < -0.2)
            {   elevAmount=-0.2f; }
            else
            {   if (key > 0.2)
                {   elevAmount=0.2f; }
                else
                {   return; }
            }

            System.out.println("orbiting elevation..");
            cameraElevation += elevAmount * 4;
            cameraElevation = cameraElevation % 360;
            updateCameraPosition();
        } 
    }

    /** Action class for radial rotation of orbit cameras */
    private class OrbitRadiusAction extends AbstractInputAction
    {   
        private float speed;
        OrbitRadiusAction(float spd) {
            speed = spd;
            //super();
        }
        

        public void performAction(float time, Event event)
        {   float radAmount;
            float key = event.getValue() * speed;
            if (key < -0.2)
            {   radAmount=-0.02f; }
            else
            {   if (key > 0.2)
                {   radAmount=0.02f; }
                else
                {   return; }
            }

            System.out.println("orbiting radius..");
            cameraRadius += radAmount * 4;
            cameraRadius = cameraRadius % 360;
            updateCameraPosition();
        } 
    }

    public void updateAzimuth(float amount) {
        cameraAzimuth += amount;
        updateCameraPosition();
    }

    public void updateElevation(float amount) {
        cameraElevation += amount;
        updateCameraPosition();
    }

    public void updateRadius(float amount) {
        cameraRadius += amount;
        updateCameraPosition();
    }
}
