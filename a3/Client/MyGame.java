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
 * -car camera movements (in CameraOrbit3D.java)
 * -clean up code (remove all the empty imports lol, move the var declarations to the tippy top :> )
 */

package a3.Client;

// Tage
import tage.*;
import tage.Light.LightType;
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
import java.util.Arrays;

import jogamp.opengl.awt.VersionApplet;

public class MyGame extends VariableFrameRateGame
{	private InputManager im;
	private MouseWheelListener ml;
	private static GhostManager gm;
	private static Engine engine;
	private static Camera mainCamera;

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
	private GameObject nearest, avatar, tree, terr, terr2, terr3, terr4, terr5, sphere, cube, tor, xLine, yLine, zLine, xLine2, yLine2, zLine2, pyr, floor;
	private GameObject carTires;
	private ObjShape treeS, avatarS, terrS, ghostS, sphereS ,cubeS, torS, xLineS, yLineS, zLineS, pyrS, planeS, npcS;
	private AnimatedShape carS, carTiresS, treeAS;
	private TextureImage doltx, treeTx, carTx, ghostT, smallHills, cornerHill, desert, grass, spheretx, cubtx, tortx, pyrtx, boomtx, planetx, spheretx2, cubtx2, tortx2, pyrtx2, npcTx;
	private Light light1, light2, light3, light4, light5;
	Vector3f lightAbove = new Vector3f(0f,2f,0f);
	Vector3f origin = new Vector3f(0,0,0);
	private Vector3f avatarLoc;
	private int fluffyClouds; // skyboxes

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
	private String viewType = "car";
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
	private boolean showPhysics = true;

	// sound
	private IAudioManager audioMgr;
	private Sound sound1, revSound, idleSound, backRevSound, backIdleSound, bgm;
	private boolean stopRev, stopIdle, stopBackRev, stopBackIdle, treeDance = false;
	private double revNow, backRevNow = 0;

	// extra vars
    
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
		avatarS = new ImportedModel("Car Chassis.obj");
		//carS = new AnimatedShape("Car Chassis.rkm", "Car Chassis.rks");
		//carS.loadAnimation("ACCEL", "driveForward.rka");
		//carS.loadAnimation("DECEL", "driveBackward.rka");
		npcS = new ImportedModel("Demon Ball thing lol.obj");
		terrS = new TerrainPlane(1000); // pixels per axis
		ghostS = new AnimatedShape("Car Chassis.rkm", "Car Chassis.rks");
		xLineS = new Line(origin, new Vector3f(3,0,0));
		yLineS = new Line(origin, new Vector3f(0,3,0));
		zLineS = new Line(origin, new Vector3f(0,0,3));
        sphereS = new Sphere();
        cubeS = new Cube();
		torS = new Torus();
		pyrS = new Pyramid();
		planeS = new Plane();
		//treeS = new ImportedModel("Slender Silhouette Sweetgum Tree.mtl.obj"); // I made these
		treeAS = new AnimatedShape("Slender Silhouette Sweetgum Tree.rkm", "Slender Silhouette Sweetgum Tree.rks");
		treeAS.loadAnimation("DANCE", "jiggle.rka");
		tireS = new ImportedModel("Tire.obj"); // I made these

		System.out.println("ANIMATION: " + treeAS.getAnimation("DANCE") + "\nbone #: " + treeAS.getBoneCount());
		
	}

	@Override // initializes texture vars
	public void loadTextures()
	{	
		cubtx = new TextureImage("brick1.jpg"); // texture obtained from pexels.com, source: Pixabay, copyright free
		//cubtx2 = new TextureImage("rocc.jpg"); // texture obtained from pexels.com, source: Life Of Pix, copyright free
		//spheretx = new TextureImage("LilyWater.png"); // custom texture
        //spheretx2 = cubtx2;
		//tortx = new TextureImage("goofySunset.png"); // custom texture
		//tortx2 = cubtx2;
		pyrtx = cubtx; //copy over the bricks
		//pyrtx2 = cubtx2;

		// my model textures
		tireTx = new TextureImage("Tire Texture WIP.png");
		treeTx = new TextureImage("Tree Texture Image.png");
		npcTx = new TextureImage("best texure image ever.png");

		//planetx = new TextureImage("floor.jpg"); // https://www.pexels.com/photo/black-and-white-carbon-pattern-2092075/
		smallHills = new TextureImage("Height Map WIP.png"); // custom greyscale shit
		cornerHill = new TextureImage("corner heightmap.png");
		grass = new TextureImage("grah texture.png"); // custom texture
		desert = new TextureImage("desert texture.png");

		carTx = new TextureImage("car tex.png");
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
			.rotateX((float)java.lang.Math.toRadians(rotation.x()))
			.rotateY((float)java.lang.Math.toRadians(rotation.y()))
			.rotateZ((float)java.lang.Math.toRadians(rotation.z()));
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
		build(cube, new Vector3f(6,8.5f,10), new Vector3f(0.5f, 0.5f, 0.5f), new Vector3f(0,0,0));
		satelliteArray[0] = cube; // load to array for future use

		// build the background terrain!
		terr = new GameObject(GameObject.root(), terrS, grass);
		build(terr, new Vector3f(200f, -50f, 200f), new Vector3f(200.0f, 100.0f, 200.0f), new Vector3f(0f, 90f, 0f));
		terr.setHeightMap(cornerHill);
		//
		terr2 = new GameObject(GameObject.root(), terrS, grass);
		build(terr2, new Vector3f(-200f, -50f, 200f), new Vector3f(200.0f, 100.0f, 200.0f), new Vector3f(0f, 0f, 0f));
		terr2.setHeightMap(cornerHill);
		//
		terr3 = new GameObject(GameObject.root(), terrS, grass);
		build(terr3, new Vector3f(200f, -50f, -200f), new Vector3f(200.0f, 100.0f, 200.0f), new Vector3f(0f, 180f, 0f));
		terr3.setHeightMap(cornerHill);
		//
		terr4 = new GameObject(GameObject.root(), terrS, grass);
		build(terr4, new Vector3f(-200f, -50f, -200f), new Vector3f(200.0f, 100.0f, 200.0f), new Vector3f(0f, 270f, 0f));
		terr4.setHeightMap(cornerHill);

		terr5 = new GameObject(GameObject.root(), cubeS, desert);
		build(terr5, new Vector3f(-0, -4f, -0), new Vector3f(200.0f, 4.0f, 200.0f), new Vector3f(0f, 0f, 0f));
		terr5.getRenderStates().setTiling(1);
		terr5.getRenderStates().setTileFactor(10);
		//terr5.getRenderStates().hasLighting(false);

		// build the tree
		tree = new GameObject(GameObject.root(), treeAS, treeTx);
		build(tree, new Vector3f(10f, 7f, 4f), new Vector3f(2.0f, 2.0f, 2.0f), new Vector3f(0f, 0f, 0f));
		
		// build a tire
		tire1 = new GameObject(GameObject.root(), tireS, tireTx);
		build(tire1, new Vector3f(-5f, 4.5f, 14f), // calculation to place tree stump a little below the ground
			new Vector3f(3f, 3f, 3f), new Vector3f(0f, 0f, 0f));

        // build sphere
		sphere = new GameObject(GameObject.root(), sphereS, pyrtx);
        initialTranslation = (new Matrix4f()).translation(2,10.5f,-5);
        initialScale = (new Matrix4f()).scaling(0.5f);
        sphere.setLocalTranslation(initialTranslation);
        sphere.setLocalScale(initialScale);
		satelliteArray[1] = sphere; 

        // build tor
        tor = new GameObject(GameObject.root(), torS, pyrtx);
        initialTranslation = (new Matrix4f()).translation(-8,10.5f,8);
        initialScale = (new Matrix4f()).scaling(0.5f);
        tor.setLocalTranslation(initialTranslation);
        tor.setLocalScale(initialScale);
        satelliteArray[2] = tor;

		// Manual object (build pyramid)
		pyr = new GameObject(GameObject.root(), pyrS, pyrtx);
		initialTranslation = (new Matrix4f()).translation(-4,10.5f,-11);
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
		
        //build dolphin / car / avatar you get the point (no you dont) 
        avatar = new GameObject(GameObject.root(), avatarS, carTx);
		build(avatar, new Vector3f(22.6f,4f,17), new Vector3f(3f,3f,3f), new Vector3f(65, 0,0));
		//initialTranslation = (new Matrix4f()).translation(20,3.5f,10);
		//initialScale = (new Matrix4f()).scaling(3.0f);
		//initialRotation = (new Matrix4f()).identity().rotationY((float)java.lang.Math.toRadians(180f));
		//avatar.setLocalTranslation(initialTranslation);
		//avatar.setLocalScale(initialScale);
		//avatar.setLocalRotation(initialRotation);

		//carTires = new GameObject(GameObject.root(), carTiresS);
		//build(carTires, new Vector3f(22.6f,4f,17), new Vector3f(3f,3f,3f), new Vector3f(65, 0,0));
		//initialScale = (new Matrix4f()).scaling(2f);
		//carTires.setLocalScale(initialScale);

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

		resource1 = audioMgr.createAudioResource("Car Rev.wav", AudioResourceType.AUDIO_SAMPLE);
		revSound = new Sound(resource1, SoundType.SOUND_EFFECT, 100, true);
		revSound.initialize(audioMgr);
		revSound.setMaxDistance(20f);
		revSound.setMinDistance(0.5f);
		revSound.setRollOff(1f);

		resource1 = audioMgr.createAudioResource("Car Idle.wav", AudioResourceType.AUDIO_SAMPLE);
		idleSound = new Sound(resource1, SoundType.SOUND_EFFECT, 100, true);
		idleSound.initialize(audioMgr);
		idleSound.setMaxDistance(20f);
		idleSound.setMinDistance(0.5f);
		idleSound.setRollOff(1f);

		resource1 = audioMgr.createAudioResource("Back Rev.wav", AudioResourceType.AUDIO_SAMPLE);
		backRevSound = new Sound(resource1, SoundType.SOUND_EFFECT, 100, true);
		backRevSound.initialize(audioMgr);
		backRevSound.setMaxDistance(20f);
		backRevSound.setMinDistance(0.5f);
		backRevSound.setRollOff(1f);

		resource1 = audioMgr.createAudioResource("Back Idle.wav", AudioResourceType.AUDIO_SAMPLE);
		backIdleSound = new Sound(resource1, SoundType.SOUND_EFFECT, 100, true);
		backIdleSound.initialize(audioMgr);
		backIdleSound.setMaxDistance(20f);
		backIdleSound.setMinDistance(0.5f);
		backIdleSound.setRollOff(1f);

		resource1 = audioMgr.createAudioResource("BGM.wav", AudioResourceType.AUDIO_STREAM);
		bgm = new Sound(resource1, SoundType.SOUND_EFFECT, 100, true);
		bgm.initialize(audioMgr);
		bgm.setMaxDistance(20f);
		bgm.setMinDistance(0.5f);
		bgm.setRollOff(0f);
	}

	public void setEarParameters() {
		Camera camera = (engine.getRenderSystem()).getViewport("MAIN").getCamera();
		audioMgr.getEar().setLocation(avatar.getWorldLocation());
		audioMgr.getEar().setOrientation(camera.getN(), camera.getU());
	}

	@Override // init lights
	public void initializeLights()
	{	Light.setGlobalAmbient(0.3f, 0.3f, 0.3f);

        light1 = new Light();
		light1.setSpecular(0.5f, 0f, 0f);
		light1.setDiffuse(0.5f, 0f, 0f);
		light1.setAmbient(0.5f, 0f, 0f);
		light1.setLocation(new Vector3f((cube.getLocalLocation())).add(lightAbove));
		light1.setLinearAttenuation(0.1f);
		(engine.getSceneGraph()).addLight(light1);

		light2 = new Light();
		light2.setSpecular(0f, 0f, 0.5f);
		light2.setDiffuse(0f, 0f, 0.5f);
		light2.setAmbient(0f, 0f, 0.5f);
		light2.setLocation(new Vector3f((sphere.getLocalLocation())).add(lightAbove));
		light2.setLinearAttenuation(0.1f);
		(engine.getSceneGraph()).addLight(light2);

		light3 = new Light();
		light3.setSpecular(0.5f, 0.5f, 0f);
		light3.setDiffuse(0.5f, 0.5f, 0f);
		light3.setAmbient(0.5f, 0.5f, 0f);
		light3.setLocation(new Vector3f((tor.getLocalLocation())).add(lightAbove));
		light3.setLinearAttenuation(0.1f);
		(engine.getSceneGraph()).addLight(light3);

		light4 = new Light();
		light4.setSpecular(0f, 0.5f, 0f);
		light4.setDiffuse(0f, 0.5f, 0f);
		//.setAmbient(0f, 0.5f, 0f);
		light4.setLocation(new Vector3f((tor.getLocalLocation())).add(lightAbove));
		light4.setLinearAttenuation(0.1f);
		(engine.getSceneGraph()).addLight(light4);

		light5 = new Light();
		light5.setSpecular(0.9f, 0.9f, 0.9f);
		light5.setDiffuse(0.7f, 0.7f, 0.7f);
		light5.setAmbient(0.7f, 0.7f, 0.7f);
		light5.setRange(0.7f);
		light5.setType(LightType.SPOTLIGHT);
		light5.setCutoffAngle(30f);
		light5.setLinearAttenuation(0.1f);
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
		
		Image faceImage = new ImageIcon("./assets/textures/Game Crosshair.png").getImage();
		Cursor faceCursor = Toolkit.getDefaultToolkit().createCustomCursor(faceImage, new Point(0,0), "FaceCursor");
		engine.getRenderSystem().getGLCanvas().setCursor(faceCursor);
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

	private void visualize4x3d(double[] x) {
		System.out.println(" transform thing: "+ x[0] + " | " +  x[1] + " | " + x[2] + " | " + x[3]);
		System.out.println(" transform thing: "+ x[4] + " | " +  x[5] + " | " + x[6] + " | " + x[7]);
		System.out.println(" transform thing: "+ x[8] + " | " +  x[9] + " | " + x[10] + " | " + x[11]);
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
		float boxSize[] = {5, 2 , 14}; // car dimensions

		Matrix4f translation = new Matrix4f(avatar.getWorldTranslation().translate(0, 1.5f, 0.5f));
		tempTransform = toDoubleArray(translation.get(vals));
		carPO = (engine.getSceneGraph()).addPhysicsBox(6.0f, tempTransform, boxSize);
		carPO.setTransform(tempTransform);
		carPO.setBounciness(0.02f);
		carPO.setFriction(3f);
		carPO.setTransform(tempTransform);
		avatar.setPhysicsObject(carPO);

		translation = new Matrix4f(tire1.getWorldTranslation());
		tempTransform = toDoubleArray(translation.get(vals));
		tirePO = (engine.getSceneGraph()).addPhysicsCapsuleX(0.7f, tempTransform, 1.7f, 0f);
		tirePO.setBounciness(0.8f);
		tire1.setPhysicsObject(tirePO);

		translation = new Matrix4f();
		tempTransform = toDoubleArray(translation.get(vals));
		terrPO = (engine.getSceneGraph()).addPhysicsStaticPlane(tempTransform, up, 0.0f);
		terrPO.setBounciness(1.0f);
		//terr.setPhysicsObject(terrPO);
	}

	public void initializeSound() {
		sound1.setLocation(tire1.getWorldLocation());
		revSound.setLocation(avatar.getWorldLocation());
		idleSound.setLocation(avatar.getWorldLocation());
		backIdleSound.setLocation(avatar.getWorldLocation());
		backRevSound.setLocation(avatar.getWorldLocation());
		bgm.setLocation(cam.getLocation());
		setEarParameters();
		//sound1.play();
		bgm.play();
	}

	@Override // init game
	public void initializeGame()	
	{	maxDistance = 5;
		lastFrameTime = currFrameTime = System.currentTimeMillis();
		elapsTime = 0.0;
		(engine.getRenderSystem()).setWindowDimensions(1900,1000);

		// setup mouse wheel listener
		engine.getRenderSystem().getGLCanvas().addMouseWheelListener(ml);

		// ------------- initialize camera ------------------
		createViewports();
		cam = engine.getRenderSystem().getViewport("MAIN").getCamera();
		orbitController = new CameraOrbit3D(cam, avatar, engine, true);
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

		// start animations
		//System.out.println("animation: " + carS.getAnimation("ACCEL"));
		//carS.playAnimation("ACCEL", 1f, AnimatedShape.EndType.LOOP, 0);
		//treeAS.playAnimation("DANCE", 1f, AnimatedShape.EndType.LOOP, 0);

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

	// returns avatar object
	public GameObject getAvatar() { return avatar; }
	public AnimatedShape getTires() { return null; }
	public PhysicsObject getPhysCar() { return carPO; }
	public ObjShape getNPCShape() { return npcS; }
	public TextureImage getNPCtexture() { return npcTx; }
	public String getViewType() { return viewType; } 

	// returns Car object
	public Car getCar() { return car; }

	// sets sprint modifier
	public void setSprint(float e) { sprintModifier = e; }

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

			if(viewType == "player") {
				cam.yaw(delX);
				//cam.pitch(delY);
			}
			else if(viewType == "car") {
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

			if(viewType == "player") {
				cam.yaw(delX);
				//cam.pitch(delY);
			}
			else if(viewType == "car") {
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
	public void mouseWheelMoved(MouseWheelEvent e) {
		orbitController.updateRadius(e.getWheelRotation());
	}
	/* --------------------- */

	public void buildHUD() {
		// build HUD
		int elapsTimeSec = Math.round((float)elapsTime);
		String elapsTimeStr = Integer.toString(elapsTimeSec);
		String counterStr = Integer.toString(score);
		String closeEnough = "";
		String disarmed = "";

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

	public void mountHandler() {
		if(viewType == "player") { viewType = "car"; }
		else { 
			viewType = "player"; 
			Vector3f e = avatar.getWorldRightVector();
			cam.setLocation(avatar.getWorldLocation().add(-e.x, 2, -e.z));
		}
	}

	private void handlesoundStopping() {
		if(revNow != 0) {
			//System.out.println("revNow: " + revNow + "\nelapsTime " + elapsTime);
			if((revNow+3.4) < elapsTime) {
				revSound.stop();
				revNow = 0;
			}
		}
		if(backRevNow != 0) {
			if((backRevNow+1.44) < elapsTime) {
				backRevSound.stop();
				backRevNow = 0;
			}
		}
	}

	private void animationHandler() {
		if(getDistance(avatar.getWorldLocation(), tree.getWorldLocation()) < 30) { 
			if(!treeDance) {
				treeAS.playAnimation("DANCE", 1f, AnimatedShape.EndType.LOOP, 0);
				treeDance = true;
			}
		}
		else {
			treeAS.stopAnimation();
			treeDance = false;
		}
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
				break;
			case KeyEvent.VK_3:
				mountHandler();
				break;
			case KeyEvent.VK_W:
				if(!stopRev) {
					revSound.play();
					//carS.playAnimation("ACCEL", 1f, AnimatedShape.EndType.LOOP, 0);
					revNow = elapsTime;
					stopRev = true;
				}
				else if(revNow == 0 && !stopIdle) {
					System.out.println("start");
					idleSound.play();
					//carS.playAnimation("ACCEL", 1f, AnimatedShape.EndType.LOOP, 0);
					stopIdle = true;
				}
				break;
			case KeyEvent.VK_S:
				if(!stopBackRev) {
					backRevSound.play();
					//carS.playAnimation("DECEL", 1f, AnimatedShape.EndType.LOOP, 0);
					backRevNow = elapsTime;
					stopBackRev = true;
				}
				else if(backRevNow == 0 && !stopBackIdle) {
					System.out.println("start");
					backIdleSound.play();
					//carS.playAnimation("DECEL", 1f, AnimatedShape.EndType.LOOP, 0);
					stopBackIdle = true;
				}
				break;
		}
		super.keyPressed(e);
	}

	@Override
	public void keyReleased(KeyEvent e) {
		switch(e.getKeyCode()) {
			case KeyEvent.VK_W:
				if(stopIdle) {
					System.out.println("stop");
					idleSound.stop();
					//carS.stopAnimation();
					stopIdle = false;
				}
				break;
			case KeyEvent.VK_S:
				if(stopBackIdle) {
					System.out.println("stop");
					backIdleSound.stop();
					//carS.stopAnimation();
					stopBackIdle = false;
				}
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

		// updates variable actions 
		Disarm.updateObject(nearest);
		MoveAction.updateModifier(timeModifier * sprintModifier);
		TurnAction.updateModifier(timeModifier * sprintModifier);
		PitchAction.updateModifier(timeModifier * sprintModifier);
		sprintModifier = 1f; //reset sprint mod
		
		buildHUD();

		// moving light with camera
		cam = (engine.getRenderSystem().getViewport("MAIN").getCamera());
		light5.setLocation(new Vector3f((avatar.getWorldLocation().add(0,2,0))));
		light5.setDirection(avatar.getWorldForwardVector());

		// ####### DELETE LATER OR FIX OR ADAPT LOL
		// Update altitude of dolphin to hug height map
		//Vector3f loc = avatar.getWorldLocation();
		//float terHeight = terr.getHeight(loc.x(), loc.z());
		//avatar.setLocalLocation(new Vector3f(loc.x(), terHeight, loc.z()));
		
		// -camera control-
		if(viewType == "car") { orbitController.updateCameraPosition(); }
		
		//overheadController.updateCameraPosition();
		
		// Animations
		animationHandler(); // no this doesnt update lol
		// make a sep method which goes thru an array of the animations and updates them automatically, this is annoying
		//carTiresS.updateAnimation();
		//carTiresS.playAnimation("ACCEL", 1f, AnimatedShape.EndType.LOOP, 0);
		//carS.updateAnimation();
		//carS.playAnimation("ACCEL", 1f, AnimatedShape.EndType.LOOP, 0);
		treeAS.updateAnimation();

		// -input control-
		im.update((float)deltaTime);

		// -Network update protocol NUP-
		processNetworking((float)deltaTime);

		// Update sound
		sound1.setLocation(tire1.getWorldLocation());
		revSound.setLocation(avatar.getWorldLocation());
		idleSound.setLocation(avatar.getWorldLocation());
		backRevSound.setLocation(avatar.getWorldLocation());
		backIdleSound.setLocation(avatar.getWorldLocation());
		bgm.setLocation(cam.getLocation());
		setEarParameters();
		handlesoundStopping();

		//System.out.println("sound1 loc: " +sound1.getLocation() + "ear loc: " + audioMgr.getEar().getLocation());

		// tie wheels to car
		//      carTires.setLocalLocation(avatar.getWorldLocation().add(2.6f,0.5f,7)); // thank you rage animation exporter
		//      carTires.setLocalLocation(avatar.getWorldLocation());
		//carTires.setLocalRotation(avatar.getWorldRotation().rotateX((float)java.lang.Math.toRadians(65f)));
		//carTires.setLocalLocation(avatar.getWorldLocation().add(2.6f,0.5f,7)); // thank you rage animation exporter

		// Update physics
		if(togglePhysics) {
			physicsEngine.update((float)deltaTime);
		}
		AxisAngle4f aa = new AxisAngle4f();
		Matrix4f mat = new Matrix4f();
		Matrix4f mat2 = new Matrix4f().identity();
		Matrix4f mat3 = new Matrix4f().identity();
		Matrix4d mat4 = new Matrix4d();
		checkForCollisions();
		for(GameObject go:engine.getSceneGraph().getGameObjects()) {
			if(go.getPhysicsObject() != null) {
				// set translation
				mat.set(toFloatArray(go.getPhysicsObject().getTransform()));
				mat2.set(3,0,mat.m30());
				mat2.set(3,1,mat.m31());
				mat2.set(3,2,mat.m32());
				//System.out.println("po tmat:\n" + mat);
				//System.out.println("go tmat:\n" + mat2);
				go.setLocalTranslation(mat2);

				// set rotation
				mat.getRotation(aa);
				mat3.rotation(aa);
				go.setLocalRotation(mat3);
			}
		}
	}
}