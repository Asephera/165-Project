package tage.nodeControllers;
import tage.*;
import org.joml.*;
import org.joml.Math;

/**
* A ShiverController is a node controller that, when enabled, causes any object
* it is attached to to translate back and forth in place around the axis specified.
* @author Connor Yep
*/
public class ShiverController extends NodeController {
    private Vector3f translationAxis = new Vector3f(1.0f, 0.0f, 0.0f);
	private float translationSpeed = 1.0f;
	private float timeElapsed = 0;
	private Matrix4f curTranslation, transMatrix, newTranslation;
	private Engine engine;

	/** Creates a shiver controller with vertical axis, and speed=1.0. */
    public ShiverController() { super(); }
    
	/** Creates a shiver controller with a movement axis, and speed as specified. */
    public ShiverController(Engine e, Vector3f axis, float speed)
	{	super();
		translationAxis = new Vector3f(axis);
		translationSpeed = speed;
		engine = e;
		transMatrix = new Matrix4f();
	}

    /** sets the shiver speed when the controller is enabled */
	public void setSpeed(float s) { translationSpeed = s; }

	/** This is called automatically by the RenderSystem (via SceneGraph) once per frame
	*   during display().  It is for engine use and should not be called by the application.
	*/
	public void apply(GameObject go)
	{	float elapsedTime = super.getElapsedTime();
        timeElapsed += elapsedTime;

		float offset = (float) Math.sin(timeElapsed * translationSpeed) * 0.02f;
		Vector3f newPos = go.getLocalLocation().add(new Vector3f(translationAxis).mul(offset));
		go.setLocalLocation(newPos);
	}
}
