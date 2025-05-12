package a3.Client;

import java.awt.Color;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.UUID;
import java.util.Vector;
import org.joml.*;

import tage.*;
import tage.networking.client.GameConnectionClient;

public class ProtocolClient extends GameConnectionClient {
    private MyGame game;
    private GhostManager ghostManager;
    private UUID id;
	private GhostNPC ghostNPC;

    public ProtocolClient(InetAddress remoteAddr, int remotePort, ProtocolType protocolType, MyGame game) throws IOException {
        super(remoteAddr, remotePort, protocolType);
        this.game = game;
        this.id = UUID.randomUUID();
        ghostManager = game.getGhostManager();
    }

    public UUID getID() { return id; }

    @Override
    protected void processPacket(Object message) {
        String strMessage = (String)message;
        //System.out.println("message recieved -->" + strMessage);
        String[] messageTokens = strMessage.split(",");

        // Game specific protocol to handle the message
		if(messageTokens.length > 0)
		{
			// Handle JOIN message
			// Format: (join,success) or (join,failure)
			if(messageTokens[0].compareTo("join") == 0) {	
				if(messageTokens[1].compareTo("success") == 0) {	
					System.out.println("join success confirmed");
					game.setIsConnected(true);
					sendCreateMessage(game.getPlayerPosition());

					try {
						sendPacket("needNPC," + id.toString());
						System.out.println("Requested NPC from server");
					} catch (IOException e) { e.printStackTrace(); }
				}

				if(messageTokens[1].compareTo("failure") == 0) {	
					System.out.println("join failure confirmed");
					game.setIsConnected(false);
				}	
			}
			
			// Handle BYE message
			// Format: (bye,remoteId)
			if(messageTokens[0].compareTo("bye") == 0)
			{	// remove ghost avatar with id = remoteId
				// Parse out the id into a UUID
				UUID ghostID = UUID.fromString(messageTokens[1]);
				ghostManager.removeGhostAvatar(ghostID);
			}
			
			// Handle CREATE message
			// Format: (create,remoteId,x,y,z)
			// AND
			// Handle DETAILS_FOR message
			// Format: (dsfr,remoteId,x,y,z)
			if (messageTokens[0].compareTo("create") == 0 || (messageTokens[0].compareTo("dsfr") == 0))
			{	// create a new ghost avatar
				// Parse out the id into a UUID
				UUID ghostID = UUID.fromString(messageTokens[1]);
				
				// Parse out the position into a Vector3f
				Vector3f ghostPosition = new Vector3f(
					Float.parseFloat(messageTokens[2]),
					Float.parseFloat(messageTokens[3]),
					Float.parseFloat(messageTokens[4]));

				try
				{	ghostManager.createGhostAvatar(ghostID, ghostPosition);
				}	catch (IOException e)
				{	System.out.println("error creating ghost avatar");
				}
			}
			
			// Handle WANTS_DETAILS message
			// Format: (wsds,remoteId)
			if (messageTokens[0].compareTo("wsds") == 0)
			{
				// Send the local client's avatar's information
				// Parse out the id into a UUID
				UUID ghostID = UUID.fromString(messageTokens[1]);
				sendDetailsForMessage(ghostID, game.getPlayerPosition());
			}
			
			// Handle MOVE message
			// Format: (move,remoteId,x,y,z)
			if (messageTokens[0].compareTo("move") == 0)
			{
				// move a ghost avatar
				// Parse out the id into a UUID
				UUID ghostID = UUID.fromString(messageTokens[1]);
				
				// Parse out the position into a Vector3f
				Vector3f ghostPosition = new Vector3f(
					Float.parseFloat(messageTokens[2]),
					Float.parseFloat(messageTokens[3]),
					Float.parseFloat(messageTokens[4]));
				
				ghostManager.updateGhostAvatar(ghostID, ghostPosition, 0);
            }

			// Handle YAW message
			// Format: (yaw,remoteId,angle)
			if (messageTokens[0].compareTo("yaw") == 0)
			{
				// yaw a ghost avatar
				// Parse out the id into a UUID
				UUID ghostID = UUID.fromString(messageTokens[1]);
				
				// Parse out the angle into a float
				float angle = Float.parseFloat(messageTokens[2]);
				
				ghostManager.updateGhostAvatar(ghostID, null, angle);
            }

			// handle ghost create message
			if (messageTokens[0].compareTo("createNPC") == 0) {				
				// Parse out the position
				System.out.println("client recieved createNPC message!");
				Vector3f ghostPosition = new Vector3f(
					Float.parseFloat(messageTokens[1]),
					Float.parseFloat(messageTokens[2]),
					Float.parseFloat(messageTokens[3]));
				try { createGhostNPC(ghostPosition); }	
				catch (IOException e) {	System.out.println("error creating ghost npc"); }
            }

			if (messageTokens[0].equals("npcinfo")) {				
				// Parse out the position
				System.out.println("client recieved npcinfo message!");
				Vector3f ghostPosition = new Vector3f(
					Float.parseFloat(messageTokens[1]),
					Float.parseFloat(messageTokens[2]),
					Float.parseFloat(messageTokens[3]));
				double gsize = Double.parseDouble(messageTokens[4]);
				updateGhostNPC(ghostPosition, gsize);
				
            }

			if(messageTokens[0].compareTo("isnr") == 0) { 
				System.out.println("recieved isnr request");
				Vector3f npcPosition = new Vector3f(
					Float.parseFloat(messageTokens[1]),
					Float.parseFloat(messageTokens[2]),
					Float.parseFloat(messageTokens[3]));
				double criteria = Double.parseDouble(messageTokens[4]);

				Vector3f playerPos = game.getPlayerPosition();
				double distance = game.getDistance(npcPosition, playerPos);

				if(distance < criteria) {
					System.out.println("npc close");
					sendNearReply();
				}
				else { System.out.println("npc far :("); }
            } 
        }
    }

	private void createGhostNPC(Vector3f position) throws IOException {
		if(ghostNPC == null) {
			ghostNPC = new GhostNPC(0, game.getNPCShape(), game.getNPCtexture(), position);
		}
	}

	private void updateGhostNPC(Vector3f position, double gsize) {
		boolean gs;
		if(ghostNPC == null) {
			try { createGhostNPC(position); 
			} catch (IOException e) { System.out.println("error creating NPC"); }
		}
		ghostNPC.setPosition(position);
		
		// for size changes
		//if(gsize == 1.0) gs = false; 
		//else gs = true;
		//ghostNPC.setSize(gs);
	}

    // The initial message from the game client requesting to join the 
	// server. localId is a unique identifier for the client. Recommend 
	// a random UUID.
	// Message Format: (join,localId)
	
	public void sendJoinMessage()
	{	try {	
			sendPacket(new String("join," + id.toString()));
		} catch (IOException e) 
		{	e.printStackTrace();
	}	}
	
	// Informs the server that the client is leaving the server. 
	// Message Format: (bye,localId)

	public void sendByeMessage()
	{	try 
		{	sendPacket(new String("bye," + id.toString()));
		} catch (IOException e) 
		{	e.printStackTrace();
	}	}
	
	// Informs the server of the client's Avatar's position. The server 
	// takes this message and forwards it to all other clients registered 
	// with the server.
	// Message Format: (create,localId,x,y,z) where x, y, and z represent the position

	public void sendCreateMessage(Vector3f position)
	{	try 
		{	String message = new String("create," + id.toString());
			message += "," + position.x();
			message += "," + position.y();
			message += "," + position.z();
			
			sendPacket(message);
		} catch (IOException e) 
		{	e.printStackTrace();
	}	}
	
	// Informs the server of the local avatar's position. The server then 
	// forwards this message to the client with the ID value matching remoteId. 
	// This message is generated in response to receiving a WANTS_DETAILS message 
	// from the server.
	// Message Format: (dsfr,remoteId,localId,x,y,z) where x, y, and z represent the position.

	public void sendDetailsForMessage(UUID remoteId, Vector3f position)
	{	try 
		{	String message = new String("dsfr," + remoteId.toString() + "," + id.toString());
			message += "," + position.x();
			message += "," + position.y();
			message += "," + position.z();
			
			sendPacket(message);
		} catch (IOException e) 
		{	e.printStackTrace();
	}	}
	
	// Informs the server that the local avatar has changed position.  
	// Message Format: (move,localId,x,y,z) where x, y, and z represent the position.

	public void sendMoveMessage(Vector3f position)
	{	try 
		{	String message = new String("move," + id.toString());
			message += "," + position.x();
			message += "," + position.y();
			message += "," + position.z();
			
			sendPacket(message);
		} catch (IOException e) 
		{	e.printStackTrace();
	}	}

	// Informs the server that the local avatar has changed yaw rotation.  
	// Message Format: (yaw,localId,angle) where angle is the rotation about the Y axis.

	public void sendYawMessage(float angle)
	{	try 
		{	String message = new String("yaw," + id.toString());
			message += "," + angle;
			
			sendPacket(message);
		} catch (IOException e) 
		{	e.printStackTrace();
	}	}

	public void sendNearReply() {
		try {	
			String message = new String("isNear," + id.toString());
			sendPacket(message);
		} catch (IOException e) { e.printStackTrace(); }
	}
}
