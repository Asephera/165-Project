/* MyGame.java
 * 
 * Details:
 * -Connor Yep
 * -Professor Gordon
 * -CSC 165 - 01
 * -March 6, 2025
 * 
 * Notes:
 * finally got the fucking input manager to detect my keyboard (windows 11 nightmare)
 * 
 * Incomplete:
 * -finish inputs (especially gamepad)
 * -orbit camera movements (in CameraOrbit3D.java)
 * -clean up code (remove all the empty imports lol, move the var declarations to the tippy top :> )
 */

package a3.Client;

// Tage
import tage.*;
import tage.audio.*;
import tage.shapes.*;
import tage.nodeControllers.*;
import tage.input.*; 
import tage.input.action.*;
import tage.networking.IGameConnection.ProtocolType;
import static tage.VariableFrameRateGame.*;
import tage.physics.*;
import tage.physics.JBullet.JBulletPhysicsEngine;
import tage.physics.JBullet.JBulletPhysicsObject;

// Java and J libraries
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import net.java.games.input.*;
import net.java.games.input.Component.Identifier.*;
import com.bulletphysics.*;
import javax.swing.*;
import org.joml.*;
import java.lang.Math;
import java.net.InetAddress;
import java.net.ProtocolException;
import java.net.UnknownHostException;
import jogamp.opengl.awt.VersionApplet;

public class MyGame extends VariableFrameRateGame
{	private InputManager im;
	private GhostManager gm;
	private static Engine engine;

    // Functinoal Vars
    private boolean riding=true;
	private float moveSpeed=0.04f; // how fast things move
	private float turnSpeed=0.8f; // how fast things turn
	private double lastFrameTime, currFrameTime, deltaTime, timeModifier, elapsTime; // track time
	private double sprintModifier = 1;
	private int score, i; // ## potential game breaking bug - non localized i for for loops, might cause issues if multiple loops happen at the same time
	int index = -1;
	private float worldSize = 18f;
	private int numSatelites = 4;
	private boolean checkWin;
	private boolean win, lose;
	private RotationController roCon;
	private ShiverController shiCon;
	private CameraOrbit3D orbitController, overheadController;
	private String gpName;

	// Object vars
	private Car car;
	private GameObject nearest, avatar, tree, terr, sphere, cube, tor, xLine, yLine, zLine, xLine2, yLine2, zLine2, pyr, floor;
	private ObjShape carSXXXXXXXXXXXXXXX, treeS, terrS, ghostS, sphereS ,cubeS, torS, xLineS, yLineS, zLineS, pyrS, planeS;
	private TextureImage doltx, treeTx, ghostT, hills, grass, spheretx, cubtx, tortx, pyrtx, boomtx, planetx, spheretx2, cubtx2, tortx2, pyrtx2;
	private Light light1, light2, light3, light4, light5;
	Vector3f lightAbove = new Vector3f(0f,2f,0f);
	Vector3f origin = new Vector3f(0,0,0);
	private Vector3f avatarLoc;
	private int fluffyClouds; // skyboxes
	private AnimatedShape carS;

	// Object vars 2 (will fix latr) (car things)
	private GameObject frontDoor1, backDoor1, engine1, tire1, spoiler1;
	private ObjShape frontLeftDoorS, frontRightDoorS, backLeftDoorS, backRightDoorS, tireS, spoilerS, engineS;
	private TextureImage frontLeftDoorTx, frontRightDoorTx, backLeftDoorTx, backRightDoorTx, tireTx, spoilerTx, engineTx;

	// Physics 
	private PhysicsEngine physicsEngine;
	private PhysicsObject carPO, terrPO, tirePO;

	// Object types
	// +++++ Consider carFrontLeft / carFrontRight door etc
	private String carFrontDoor = "carFrontDoor";
	private String carBackDoor = "carBackDoor";
	private String carEngine = "carEngine";
	private String carTire = "carTire";
	private String carSpoiler = "carSpoiler";

	// Arrays
	private GameObject[] partsArray = new GameObject[numSatelites];
	private GameObject[] satelliteArray = new GameObject[numSatelites]; // aeeay of satellites
	private boolean[] disarm = new boolean[numSatelites]; // disarm bool per satellite
	private boolean[] exploded = new boolean[numSatelites];
	private TextureImage[][] textureArray = new TextureImage[numSatelites][2]; // 2 textures per satellite

    // Camera Control
    private Vector3f loc, fwd, up, right;
    private Camera cam;
	private float maxDistance;

	// Mouse movement variables
	private boolean recentering;
	private String viewType = "orbit";
	private int centerX, centerY;
	private float curX, curY, prevX, prevY;
	private float sensitivity = 0.1f;
	private Robot robot;

	// networking
	private String serverAddress;
	private int serverPort;
	private ProtocolType serverProtocol;
	private ProtocolClient protClient;
	private boolean isClientConnected = false;

	// vars I need to toggle with buttons
	private boolean togglePhysics = true;
	private boolean showPhysics = false;

	// sound
	private IAudioManager audioMgr;
	private Sound sound1;
    
	public MyGame(String serverAddress, int serverPort, String protocol) { 
		super(); 
		gm = new GhostManager(this);
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
		this.serverProtocol = ProtocolType.UDP;
	}

	public static void main(String[] args)
	{	MyGame game = new MyGame(args[0], Integer.parseInt(args[1]), args[2]);
        engine = new Engine(game);
        game.initializeSystem();
		game.game_loop();
	}

	@Override
	public void loadShapes()
	{	
		carS = new AnimatedShape("Car Chassis.rkm", "Car Chassis.rks"); //new ImportedModel("Car Chassis Blockout.obj");
		carS.loadAnimation("ACCEL", "accelerate.rka");
		terrS = new TerrainPlane(1000); // pixels per axis
		ghostS = new Sphere();
		xLineS = new Line(origin, new Vector3f(3,0,0));
		yLineS = new Line(origin, new Vector3f(0,3,0));
		zLineS = new Line(origin, new Vector3f(0,0,3));
        sphereS = new Sphere();
        cubeS = new Cube();
		torS = new Torus();
		pyrS = new Pyramid();
		planeS = new Plane();
		treeS = new ImportedModel("Slender Silhouette Sweetgum Tree.mtl.obj"); // I made these
		tireS = new ImportedModel("Tire.obj"); // I made these
	}

	@Override // initializes texture vars
	public void loadTextures()
	{	
		doltx = new TextureImage("Dolphin_HighPolyUV.jpg");
		cubtx = new TextureImage("brick.jpg"); // texture obtained from pexels.com, source: Pixabay, copyright free
		cubtx2 = new TextureImage("rocc.jpg"); // texture obtained from pexels.com, source: Life Of Pix, copyright free
		spheretx = new TextureImage("LilyWater.png"); // custom texture
        spheretx2 = cubtx2;
		tortx = new TextureImage("goofySunset.png"); // custom texture
		tortx2 = cubtx2;
		pyrtx = cubtx; //copy over the bricks
		pyrtx2 = cubtx2;

		// my model textures
		tireTx = new TextureImage("Tire Texture WIP.png");
		treeTx = new TextureImage("Tree Texture Image.png");

		planetx = new TextureImage("floor.jpg"); // https://www.pexels.com/photo/black-and-white-carbon-pattern-2092075/
		hills = new TextureImage("Height Map WIP.png"); // custom greyscale shit
		grass = new TextureImage("grah texture.png"); // custom texture
		ghostT = new TextureImage("brick1.jpg"); // (for now: bricks) I assume I just make this the normal player model but with differently colored shirts or something

		boomtx = new TextureImage("boom.jpg"); // texture obtained from pexels.com, source: Pixabay, copyright free

		// load textures into array for future use
		textureArray[0][0] = cubtx;
		textureArray[0][1] = cubtx2;
		textureArray[1][0] = spheretx;
		textureArray[1][1] = spheretx2;
		textureArray[2][0] = tortx;
		textureArray[2][1] = tortx2;
		textureArray[3][0] = pyrtx;
		textureArray[3][1] = pyrtx2;
	}

	// abstracts translation and scaling of objects
	public void build(GameObject obj, Vector3f location, Vector3f scale, Vector3f rotation) {
		Matrix4f initialTranslation, initialScale, initialRotation;

        initialTranslation = (new Matrix4f()).translation(location);
        initialScale = (new Matrix4f()).scaling(scale);
		initialRotation = (new Matrix4f()).identity()
			.rotationX((float)java.lang.Math.toRadians(rotation.x()))
			.rotationY((float)java.lang.Math.toRadians(rotation.y()))
			.rotationZ((float)java.lang.Math.toRadians(rotation.z()));
        obj.setLocalTranslation(initialTranslation);
        obj.setLocalScale(initialScale);
		obj.setLocalRotation(initialRotation);
	}
	
	@Override // builds out objects with shape and textures
	public void buildObjects()
	{	Matrix4f initialTranslation, initialScale, initialRotation; // ##### to be removed 

        // build cube
		// ##### Abstracted for just some so far, rest are still grandfathered with old method
        cube = new GameObject(GameObject.root(), cubeS, cubtx);
		build(cube, new Vector3f(6,0.5f,10), new Vector3f(0.5f, 0.5f, 0.5f), new Vector3f(0,0,0));
		satelliteArray[0] = cube; // load to array for future use

		// build terrain object
		terr = new GameObject(GameObject.root(), terrS, grass);
		build(terr, new Vector3f(0f, 0f, 0f), new Vector3f(20.0f, 4.0f, 20.0f), new Vector3f(0f, 0f, 0f));
		terr.setHeightMap(hills);

		// build a tree
		tree = new GameObject(GameObject.root(), treeS, treeTx);
		build(tree, new Vector3f(10f, 2f, 4f), // calculation to place tree stump a little below the ground
			new Vector3f(2.0f, 2.0f, 2.0f), new Vector3f(0f, 0f, 0f));
		
		// build a tire
		tire1 = new GameObject(GameObject.root(), tireS, tireTx);
		build(tire1, new Vector3f(-5f, 4.5f, 14f), // calculation to place tree stump a little below the ground
			new Vector3f(3f, 3f, 3f), new Vector3f(0f, 0f, 0f));

        // build sphere
		sphere = new GameObject(GameObject.root(), sphereS, spheretx);
        initialTranslation = (new Matrix4f()).translation(2,0.5f,-5);
        initialScale = (new Matrix4f()).scaling(0.5f);
        sphere.setLocalTranslation(initialTranslation);
        sphere.setLocalScale(initialScale);
		satelliteArray[1] = sphere; 

        // build tor
        tor = new GameObject(GameObject.root(), torS, tortx);
        initialTranslation = (new Matrix4f()).translation(-8,0.5f,8);
        initialScale = (new Matrix4f()).scaling(0.5f);
        tor.setLocalTranslation(initialTranslation);
        tor.setLocalScale(initialScale);
        satelliteArray[2] = tor;

		// Manual object (build pyramid)
		pyr = new GameObject(GameObject.root(), pyrS, pyrtx);
		initialTranslation = (new Matrix4f()).translation(-4,0.5f,-11);
        initialScale = (new Matrix4f()).scaling(0.5f);
		pyr.setLocalTranslation(initialTranslation);
        pyr.setLocalScale(initialScale);
		satelliteArray[3] = pyr;

		// add X Y Z world axes
		xLine = new GameObject(GameObject.root(), xLineS);
		yLine = new GameObject(GameObject.root(), yLineS);
		zLine = new GameObject(GameObject.root(), zLineS);
		(xLine.getRenderStates()).setColor(new Vector3f(1f, 0f, 0f));
		(yLine.getRenderStates()).setColor(new Vector3f(0f, 1f, 0f));
		(zLine.getRenderStates()).setColor(new Vector3f(0f, 0f, 1f));

		// add floor plane
		floor = new GameObject(GameObject.root(), planeS, planetx);
		initialTranslation = (new Matrix4f()).translation(0,0,0);
        initialScale = (new Matrix4f()).scaling(worldSize);
		floor.setLocalTranslation(initialTranslation);
        floor.setLocalScale(initialScale);
		
        //build dolphin / car / avatar you get the point (no you dont) 
        avatar = new GameObject(GameObject.root(), carS, treeTx);
		initialTranslation = (new Matrix4f()).translation(0,3.5f,10);
		initialScale = (new Matrix4f()).scaling(3.0f);
		initialRotation = (new Matrix4f()).identity().rotationY(
			(float)java.lang.Math.toRadians(180f));
		avatar.setLocalTranslation(initialTranslation);
		avatar.setLocalScale(initialScale);
		avatar.setLocalRotation(initialRotation);

		// add mini avatar X Y Z world axes (constructed with 'satellite parts')
		xLine2 = new GameObject(avatar, xLineS);
		yLine2 = new GameObject(xLine2, yLineS);
		zLine2 = new GameObject(yLine2, zLineS);
		(xLine2.getRenderStates()).setColor(new Vector3f(1f, 0f, 0f));
		(yLine2.getRenderStates()).setColor(new Vector3f(0f, 1f, 0f));
		(zLine2.getRenderStates()).setColor(new Vector3f(0f, 0f, 1f));
		initialTranslation = (new Matrix4f()).translation(0.3f,0.7f, 0f);
        initialScale = (new Matrix4f()).scaling(.03f);
		xLine2.setLocalTranslation(initialTranslation);
		xLine2.setLocalScale(initialScale);
		xLine2.propagateRotation(false);
		xLine2.applyParentRotationToPosition(true);
		partsArray[0] = xLine2;
		partsArray[1] = yLine2;
		partsArray[2] = zLine2;
		xLine2.getRenderStates().disableRendering();
		yLine2.getRenderStates().disableRendering();
		zLine2.getRenderStates().disableRendering();
    }

	// +++++++++++++ Make sure to replace this with the skybox you want to actually use
	@Override // loads the skybox cubemap for use
	public void loadSkyBoxes() {
		fluffyClouds = (engine.getSceneGraph().loadCubeMap("fluffyClouds"));
		(engine.getSceneGraph()).setActiveSkyBoxTexture(fluffyClouds);
		(engine.getSceneGraph()).setSkyBoxEnabled(true);
	} 

	@Override
	public void loadSounds() {
		AudioResource resource1;
		audioMgr = engine.getAudioManager();
		resource1 = audioMgr.createAudioResource("tire noises.wav", AudioResourceType.AUDIO_SAMPLE);
		sound1 = new Sound(resource1, SoundType.SOUND_EFFECT, 100, true);
		sound1.initialize(audioMgr);
		sound1.setMaxDistance(20f);
		sound1.setMinDistance(0.5f);
		sound1.setRollOff(1f);
	}

	public void setEarParameters() {
		Camera camera = (engine.getRenderSystem()).getViewport("MAIN").getCamera();
		audioMgr.getEar().setLocation(avatar.getWorldLocation());
		audioMgr.getEar().setOrientation(camera.getN(), new Vector3f(0f,1f,0f));
	}

	@Override // init lights
	public void initializeLights()
	{	Light.setGlobalAmbient(0.3f, 0.3f, 0.3f);

        light1 = new Light();
		light1.setSpecular(0.5f, 0f, 0f);
		light1.setDiffuse(0.5f, 0f, 0f);
		//light1.setAmbient(0.5f, 0f, 0f);
		light1.setLocation(new Vector3f((cube.getLocalLocation())).add(lightAbove));
		(engine.getSceneGraph()).addLight(light1);

		light2 = new Light();
		light2.setSpecular(0f, 0f, 0.5f);
		light2.setDiffuse(0f, 0f, 0.5f);
		//light2.setAmbient(0f, 0f, 0.5f);
		light2.setLocation(new Vector3f((sphere.getLocalLocation())).add(lightAbove));
		(engine.getSceneGraph()).addLight(light2);

		light3 = new Light();
		light3.setSpecular(0.5f, 0.5f, 0f);
		light3.setDiffuse(0.5f, 0.5f, 0f);
		//.setAmbient(0.5f, 0.5f, 0f);
		light3.setLocation(new Vector3f((tor.getLocalLocation())).add(lightAbove));
		(engine.getSceneGraph()).addLight(light3);

		light4 = new Light();
		light4.setSpecular(0f, 0.5f, 0f);
		light4.setDiffuse(0f, 0.5f, 0f);
		//.setAmbient(0f, 0.5f, 0f);
		light4.setLocation(new Vector3f((tor.getLocalLocation())).add(lightAbove));
		(engine.getSceneGraph()).addLight(light4);

		light5 = new Light();
		light5.setSpecular(0.7f, 0.7f, 0.7f);
		light5.setDiffuse(0.7f, 0.7f, 0.7f);
		//light4.setAmbient(0.3f, 0.3f, 0.3f);
		cam = (engine.getRenderSystem().getViewport("MAIN").getCamera());
		light5.setLocation(new Vector3f((cam.getLocation())));
		(engine.getSceneGraph()).addLight(light5);
	}

	// ----- Input Manager -----
	public void initializeIM()
	{	im = engine.getInputManager();
		gpName = im.getFirstGamepadName();
		System.out.println("initializing input manager");

		// Actions
		MoveAction fwdAction = new MoveAction(this, moveSpeed, protClient);
		MoveAction bkwdAction = new MoveAction(this, -moveSpeed, protClient);
		TurnAction turnLeft = new TurnAction(this, turnSpeed, protClient);
		TurnAction turnRight = new TurnAction(this, -turnSpeed, protClient);
		Disarm disarmSattelite = new Disarm(this, nearest);
		Sprint sprint = new Sprint(this, 1.8f);
		PitchAction pitchUp = new PitchAction(this, turnSpeed);
		PitchAction pitchDown = new PitchAction(this, -turnSpeed);
		ToggleXYZ toggleXYZ = new ToggleXYZ(this, xLine, yLine, zLine);
		
		// Linking
		// ----------------------- gamepad controls -----------------------
		// movement
		if(gpName != null) { 
			im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Axis.Y, bkwdAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Axis.X, turnRight, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Button._1, disarmSattelite, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
			im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Button._5, sprint, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Button._3, toggleXYZ, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		}

		// ----------------------- Keyboard controls ----------------------
		// movement
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.W, fwdAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.S, bkwdAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.A, turnLeft, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.D, turnRight, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.UP, pitchUp, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.DOWN, pitchDown, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		// actions
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.E, disarmSattelite, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.LSHIFT, sprint, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.M, toggleXYZ, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		
	}

	// ----- Networking -----
	public ObjShape getGhostShape() { return ghostS; }
	public TextureImage getGhostTexture() { return ghostT; }
	public GhostManager getGhostManager() { return gm; }
	public Engine getEngine() { return engine; }
	public Vector3f getPlayerPosition() { return avatar.getWorldLocation(); } // ##### Change to USER location when that exists
	public void setIsConnected(boolean value) { 
		this.isClientConnected = value; 
		System.out.println("connected? " + value);
	}
	private void setupNetworking() {
		isClientConnected = false;
		try { protClient = new ProtocolClient(InetAddress.getByName(serverAddress), serverPort, serverProtocol, this); }
		catch (UnknownHostException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }

		if (protClient == null) { System.out.println("missing protocol host"); }
		else {
			System.out.println("sending join message to protocol host");
			protClient.sendJoinMessage();
		}
	}
	protected void processNetworking(float elapsTime) {
		if (protClient != null)
			protClient.processPackets();
	}
	private class SendCloseConnectionPacketAction extends AbstractInputAction
	{	@Override
		public void performAction(float time, net.java.games.input.Event evt) 
		{	if(protClient != null && isClientConnected == true)
			{	protClient.sendByeMessage();
			}
		}
	}

	// ----- Mouse Tracking -----
	public void initializeMouseMovement() {
		Viewport vw = engine.getRenderSystem().getViewport("MAIN");
		float left = vw.getActualLeft();
		float bottom = vw.getActualBottom();
		float width = vw.getActualWidth();
		float height = vw.getActualHeight();
		System.out.println("left: " + left + "bottom: " + bottom + "width: " + width + "height: " + height);
		centerX = (int) (left + width/2);
		centerY = (int) (bottom - height/2); 

		
		recentering = false;
		try // note that some platforms may not support the Robot class
		{ robot = new Robot(); } catch (AWTException ex)
		{ throw new RuntimeException("Couldn't create Robot!"); }
		recenterMouse();
		prevX = centerX; // 'prevMouse' defines the initial
		prevY = centerY; // mouse position
		

		/* ADD CURSOR
		Image faceImage = new
		ImageIcon("./assets/textures/face.gif").getImage();
		Cursor faceCursor = Toolkit.getDefaultToolkit().
		createCustomCursor(faceImage, new Point(0,0), "FaceCursor");
		canvas = rs.getCanvas();
		canvas.setCursor(faceCursor);
		*/
	}

	private float[] toFloatArray(double[] arr) {
		if(arr ==null) return null;
		int n = arr.length;
		float[] ret = new float[n];
		for(int i = 0; i<n; i++) { ret[i] = (float)arr[i]; }
		return ret;
	}

	private double[] toDoubleArray(float[] arr) {
		if(arr ==null) return null;
		int n = arr.length;
		double[] ret = new double[n];
		for(int i = 0; i<n; i++) { ret[i] = (double)arr[i]; }
		return ret;
	}

	public void initializePhysics() {
		// Gravity n physics system
		float[] gravity = {0f, -9.8f, 0f};
		physicsEngine = (engine.getSceneGraph()).getPhysicsEngine();
		physicsEngine.setGravity(gravity);

		// Physics world temp vars
		float up[] = {0, 1, 0};
		double[] tempTransform;
		float vals[] = new float[16];
		float boxSize[] = {5, 4 , 14}; // car dimensions

		Matrix4f translation = new Matrix4f(avatar.getLocalTranslation());
		tempTransform = toDoubleArray(translation.get(vals));
		carPO = (engine.getSceneGraph()).addPhysicsBox(4.0f, tempTransform, boxSize);
		carPO.setBounciness(0.02f);
		carPO.setFriction(0.1f);
		avatar.setPhysicsObject(carPO);

		translation = new Matrix4f(tire1.getLocalTranslation());
		tempTransform = toDoubleArray(translation.get(vals));
		tirePO = (engine.getSceneGraph()).addPhysicsCapsuleX(0.7f, tempTransform, 1.7f, 0f);
		tirePO.setBounciness(0.8f);
		tire1.setPhysicsObject(tirePO);

		translation = new Matrix4f(terr.getLocalTranslation().translate(0,1,0));
		tempTransform = toDoubleArray(translation.get(vals));
		terrPO = (engine.getSceneGraph()).addPhysicsStaticPlane(tempTransform, up, 0.0f);
		terrPO.setBounciness(1.0f);
		terr.setPhysicsObject(terrPO);
	}

	public void initializeSound() {
		sound1.setLocation(tire1.getWorldLocation());
		setEarParameters();
		sound1.play();
	}

	@Override // init game
	public void initializeGame()
	{	maxDistance = 5;
		lastFrameTime = currFrameTime = System.currentTimeMillis();
		elapsTime = 0.0;
		(engine.getRenderSystem()).setWindowDimensions(1900,1000);
	
		// ------------- initialize camera ------------------
		createViewports();
		Camera c = engine.getRenderSystem().getViewport("MAIN").getCamera();
		orbitController = new CameraOrbit3D(c, avatar, engine, true);
		// overhead cam
		//Camera o = engine.getRenderSystem().getViewport("overhead").getCamera();
		//overheadController = new CameraOrbit3D(o, avatar, engine, false);

		
		// ------------- positioning the camera -------------
		(engine.getRenderSystem().getViewport("MAIN").getCamera()).setLocation(new Vector3f(0,0,10));
		
		// ------------- node controllers ------------------
		roCon = new RotationController(engine, new Vector3f(0, 1, 0), 0.001f);
		(engine.getSceneGraph()).addNodeController(roCon);
		roCon.enable();
		shiCon = new ShiverController(engine, new Vector3f(0, 0, 1), 0.01f);
		(engine.getSceneGraph()).addNodeController(shiCon);
		shiCon.enable();

		System.out.println("animation: " + carS.getAnimation("ACCEL"));
		carS.playAnimation("ACCEL", 1f, AnimatedShape.EndType.LOOP, 0);

		// ----- Networking -----
		System.out.println("initializing networking..");
		setupNetworking();
		// ----- Input manager -----
		System.out.println("initializing input manager..");
		initializeIM();
		// ----- Mouse tracking -----
		System.out.println("initializing mouse movement..");
		initializeMouseMovement();
		// ----- Physics! -----
		System.out.println("initializing physics..");
		initializePhysics();
		// ----- Sound -----
		System.out.println("initializing sound..");
		initializeSound();
	}

	@Override
	public void createViewports() {
		(engine.getRenderSystem()).addViewport("MAIN",0,0,1,1);
		
		/* Overhead cam is unecessary for my game now
		(engine.getRenderSystem()).addViewport("overhead",0.75f,0f,0.25f,0.35f);
		
		Viewport overVp = (engine.getRenderSystem()).getViewport("overhead");
		Camera overCam = overVp.getCamera();

		overVp.setHasBorder(true);
		overVp.setBorderWidth(2);
		overVp.setBorderColor(0.0f, 1.0f, 1.0f);

		overCam.setLocation((new Vector3f(0, 20, 0)));
		overCam.setU(new Vector3f(1, 0, 0));
		overCam.setV(new Vector3f(0, 0, -1));
		overCam.setN(new Vector3f(0, -1, 0));
		*/
	}

	// check if the camera moves too far from avatar and fixes location if so.
	public boolean cameraBounding(Camera c, float spd) {
		float newDist = getDistance(avatar.getWorldLocation(), c.getLocation().add(c.getN().mul(spd)));
		return (newDist < maxDistance);
	}

	// fixes avatar location to world bounds
	public boolean avatarBounding(float spd) {
		Vector3f nextStep = avatar.getWorldLocation().add(avatar.getWorldForwardVector().mul(spd));
		return (0 < nextStep.y) && (worldSize > nextStep.z) && (worldSize > nextStep.x);
	}

	// returns distance between two points in 3D vector space
	public float getDistance(Vector3f vec1, Vector3f vec2) {
		return (float)(Math.sqrt(Math.pow((vec2.x() - vec1.x()), 2) + Math.pow((vec2.y() - vec1.y()), 2) + Math.pow((vec2.z() - vec1.z()), 2)));
	}

	// returns reference to nearest satellite registered in satelliteArray[]
	public GameObject getNearestSatellite() 
	{	avatarLoc = avatar.getWorldLocation();
		nearest = satelliteArray[0];
		for(i = 0; i < satelliteArray.length; i++)
		{	if(getDistance(avatarLoc, nearest.getWorldLocation()) > getDistance(avatarLoc, satelliteArray[i].getWorldLocation())) { nearest = satelliteArray[i]; } }
		return nearest;
	}

	// returns avatar object
	public GameObject getAvatar() { return avatar; }
	public AnimatedShape getTires() { return carS; }
	public PhysicsObject getPhysCar() { return carPO; }

	// returns Car object
	public Car getCar() { return car; }

	// sets sprint modifier
	public void setSprint(float e) { sprintModifier = e; }

	// returns true if passed object is close enough to cam or avatar
	public boolean closeEnough(GameObject x) {
		//cam = (engine.getRenderSystem().getViewport("MAIN").getCamera());
		if((3 > getDistance(avatar.getWorldLocation(), x.getWorldLocation()))) { 
			return true;
		}
		return false;
	}

	// returns index to satellite Array if satellite is armed, -1 if disarmed, -2 if exploded.
	public int checkSatelliteStatus(GameObject object) {
		index = -1;

		for(int k = 0; k < satelliteArray.length; k++) {
			if(satelliteArray[k] == object) { index = k; }
		}
		if(index != -1) { 
			if(!disarm[index] && !exploded[index]) { return index; }
			else if(exploded[index]) { return -2; }
		}
		//System.out.println("no");
		return -1; // satellite isnt armed
	}

	// disarm nearest satellite if in range
	public void disarm(GameObject object) {
		int j = -1;
		j = checkSatelliteStatus(object);
		if(j >= 0 && closeEnough(object)) { 
			disarm[j] = true; 
			score++; 
			if(partsArray[j] != null) { partsArray[j].getRenderStates().enableRendering(); }
			if( j % 2 == 0)
				{ shiCon.addTarget(object); }
			else
				{ roCon.addTarget(object); }
		}
		win = checkIfWin();
	}

	// if all satellites are disarmed, returns true
	public boolean checkIfWin() {
		checkWin = true;
		for(i = 0; i < disarm.length; i++) { 
			if(!disarm[i]) { checkWin = false; } 
		}
		return checkWin;
	}

	// true if avatar is further than x from object
	public boolean furtherThan(int x, GameObject object) {
		if(x > getDistance(object.getWorldLocation(), avatar.getLocalLocation())) { return true; }
		return false;
	}

	// tracks distance from avatar to each satellite and updates texture / shape accordingly
	public void satelliteCollision()
	{	avatarLoc = avatar.getLocalLocation();
		
		// if x sattelite is <4 or <2 sets texture appropriately
		for(i = 0; i < satelliteArray.length; i++) {
			if(exploded[i]) 
				{ lose = true; continue; } // if a satellite is ever blown up, its texture wont change anymore, also lose = true
			if(checkSatelliteStatus(satelliteArray[i]) == -1) 
				{ satelliteArray[i].setTextureImage(textureArray[i][0]); continue; } // if disarmed, stays the normal texture
			
			satelliteArray[i].setTextureImage(
				(furtherThan(3, satelliteArray[i])) ? 
				(furtherThan(2, satelliteArray[i])) && !disarm[i] ? 
				boomtx : textureArray[i][1] : textureArray[i][0]); // if dist <2 set texture to boom. <3 set texture to second texture. Otherwise normal texture
			
				// if dist <2 set shape to square to better visualize boom
			if(2 > getDistance(satelliteArray[i].getLocalLocation(), avatarLoc) && !disarm[i]) 
			{ 	satelliteArray[i].setShape(cubeS); 
				exploded[i] = true; }
		}
	}

	// physics world collision checking
	private void checkForCollisions()
	{	com.bulletphysics.dynamics.DynamicsWorld dynamicsWorld;
		com.bulletphysics.collision.broadphase.Dispatcher dispatcher;
		com.bulletphysics.collision.narrowphase.PersistentManifold manifold;
		com.bulletphysics.dynamics.RigidBody object1, object2;
		com.bulletphysics.collision.narrowphase.ManifoldPoint contactPoint;

		dynamicsWorld = ((JBulletPhysicsEngine)physicsEngine).getDynamicsWorld();
		dispatcher = dynamicsWorld.getDispatcher();
		int manifoldCount = dispatcher.getNumManifolds();
		for (int i=0; i < manifoldCount; i++)
		{	manifold = dispatcher.getManifoldByIndexInternal(i);
			object1 = (com.bulletphysics.dynamics.RigidBody)manifold.getBody0();
			object2 = (com.bulletphysics.dynamics.RigidBody)manifold.getBody1();
			JBulletPhysicsObject obj1 = JBulletPhysicsObject.getJBulletPhysicsObject(object1);
			JBulletPhysicsObject obj2 = JBulletPhysicsObject.getJBulletPhysicsObject(object2);
			for (int j = 0; j < manifold.getNumContacts(); j++)
			{	contactPoint = manifold.getContactPoint(j);
				if (contactPoint.getDistance() < 0.0f)
				{	//System.out.println("---- hit between " + obj1 + " and " + obj2);
					break;
				}
			}
		}
	}

	/* MOUSE CAMERA MOVEMENT */
	private void recenterMouse() {
		RenderSystem rs = engine.getRenderSystem();
		Viewport vw = rs.getViewport("MAIN");
		float left = vw.getActualLeft();
		float bottom = vw.getActualBottom();
		float width = vw.getActualWidth();
		float height = vw.getActualHeight();
		centerX = (int) (left + width/2.0f);
		centerY = (int) (bottom - height/2.0f);
		recentering = true;
		robot.mouseMove((int)centerX, (int)centerY);
	}

	/* - */
	@Override
    public void mouseMoved(MouseEvent e) {
        if(recentering && centerX == e.getXOnScreen() && centerY == e.getYOnScreen()) {
			recentering = false;
		}
		else {
			curX = e.getXOnScreen();
			curY = e.getYOnScreen();
			float delX = prevX - curX;
			float delY = prevY - curY;

			if(viewType == "fpv") {
				//yaw(delX);
				//pitch(delY);
			}
			else if(viewType == "orbit") {
				orbitController.updateAzimuth(delX*sensitivity);
				orbitController.updateElevation(-delY*sensitivity);
			}
			recenterMouse();
			prevX = centerX;
			prevY = centerY;
		}
    }

	/* - */
	@Override
    public void mouseDragged(MouseEvent e) {
        if(recentering && centerX == e.getXOnScreen() && centerY == e.getYOnScreen()) {
			recentering = false;
		}
		else {
			curX = e.getXOnScreen();
			curY = e.getYOnScreen();
			float delX = prevX - curX;
			float delY = prevY - curY;

			if(viewType == "fpv") {
				//yaw(delX);
				//pitch(delY);
			}
			else if(viewType == "orbit") {
				orbitController.updateAzimuth(delX*sensitivity);
				orbitController.updateElevation(-delY*sensitivity);
			}
			recenterMouse();
			prevX = centerX;
			prevY = centerY;
		}
    }
	/* --------------------- */

	public void buildHUD() {
		// build HUD
		int elapsTimeSec = Math.round((float)elapsTime);
		String elapsTimeStr = Integer.toString(elapsTimeSec);
		String counterStr = Integer.toString(score);
		String closeEnough = "";
		String disarmed = "";
		
		// updates HUD values for active events
		if(closeEnough(nearest)) { closeEnough = "close enough!"; }
		else { closeEnough = "too far.."; }
		if(checkSatelliteStatus(nearest) == -1 && closeEnough == "close enough!") { disarmed = "disarmed! "; }
		else { disarmed = ""; }

		// set HUD
		String dispStr1 = "Time = " + elapsTimeStr + " | Score = " + counterStr + " | " + closeEnough + " | " + disarmed;
		String dispStr2 = "     ";
		String dispStr3 = "X: " + Math.round(avatar.getWorldLocation().x) + " Y: " + Math.round(avatar.getWorldLocation().y) + " Z: " + Math.round(avatar.getWorldLocation().z);
		Vector3f hud2Color = new Vector3f(0,1,1);
		Vector3f hud1Color = new Vector3f(0,1,1);
		if(win && !lose) { dispStr2 = " You Win!"; hud1Color = new Vector3f(0,1,0);}
		if(lose) { dispStr2 = "You Lose :("; hud1Color = new Vector3f(1,0,0); }

		int hud1Left = (int)engine.getRenderSystem().getViewport("MAIN").getActualLeft();
		//int hud2Left = (int)engine.getRenderSystem().getViewport("overhead").getActualLeft();

		(engine.getHUDmanager()).setHUD1(dispStr1 + dispStr2, hud1Color, hud1Left + 15, 15);
		//(engine.getHUDmanager()).setHUD2(dispStr3, hud2Color, hud2Left + 15, 15);

	}

	@Override // simple key listener for testing mostly
	public void keyPressed(KeyEvent e) {
		switch(e.getKeyCode()) {
			case KeyEvent.VK_SPACE:
				togglePhysics = !togglePhysics;
				break;
			case KeyEvent.VK_1:
				showPhysics = !showPhysics;
				// physics world visualization
				if (showPhysics) { engine.enablePhysicsWorldRender(); } 
				else { engine.disablePhysicsWorldRender(); }
				break;
			case KeyEvent.VK_2:
				carS.stopAnimation();
				carS.playAnimation("ACCEL", 1f, AnimatedShape.EndType.LOOP, 0);
				break;
		}
		super.keyPressed(e);
	}

	@Override
	public void update()
	{
		// update time info
		lastFrameTime = currFrameTime;
		currFrameTime = System.currentTimeMillis();
		deltaTime = currFrameTime - lastFrameTime;
		timeModifier = deltaTime / 15; // deltaTime made speed jump by like 15x, this fix that
		elapsTime += deltaTime / 1000.0;
		
		// updates nearest satellite
		nearest = getNearestSatellite();

		// updates variable actions 
		Disarm.updateObject(nearest);
		MoveAction.updateModifier(timeModifier * sprintModifier);
		TurnAction.updateModifier(timeModifier * sprintModifier);
		PitchAction.updateModifier(timeModifier * sprintModifier);
		sprintModifier = 1f; //reset sprint mod
		
		buildHUD();

		// moving light with camera
		cam = (engine.getRenderSystem().getViewport("MAIN").getCamera());
		light5.setLocation(new Vector3f((cam.getLocation())));

		// ####### DELETE LATER OR FIX OR ADAPT LOL
		// Update altitude of dolphin to hug height map
		//Vector3f loc = avatar.getWorldLocation();
		//float terHeight = terr.getHeight(loc.x(), loc.z());
		//avatar.setLocalLocation(new Vector3f(loc.x(), terHeight, loc.z()));
		
		// -camera control-
		orbitController.updateCameraPosition();
		//overheadController.updateCameraPosition();
		
		// Animations
		carS.updateAnimation();
		//carS.playAnimation("ACCEL", 1f, AnimatedShape.EndType.LOOP, 0);

		// -input control-
		im.update((float)deltaTime);

		// -Network update protocol NUP-
		processNetworking((float)deltaTime);

		// Update sound
		sound1.setLocation(tire1.getWorldLocation());
		setEarParameters();

		//System.out.println("sound1 loc: " +sound1.getLocation() + "ear loc: " + audioMgr.getEar().getLocation());

		// Update physics
		if(togglePhysics) {
			AxisAngle4f aa = new AxisAngle4f();
			Matrix4f mat = new Matrix4f();
			Matrix4f mat2 = new Matrix4f().identity();
			Matrix4f mat3 = new Matrix4f().identity();
			checkForCollisions();
			physicsEngine.update((float)deltaTime);
			for(GameObject go:engine.getSceneGraph().getGameObjects()) {
				if(go.getPhysicsObject() != null) {
					mat.set(toFloatArray(go.getPhysicsObject().getTransform()));
					mat2.set(3,0,mat.m30());
					mat2.set(3,1,mat.m31());
					mat2.set(3,2,mat.m32());
					go.setLocalTranslation(mat2);

					// set rotation
					mat.getRotation(aa);
					mat3.rotation(aa);
					go.setLocalRotation(mat3);
				}
			}
		}
	}
}