package a3.Server;

import java.io.IOException;

public class NetworkingServer {
    private GameServerUDP thisUDPServer;

    public NetworkingServer(int serverPort, String protocol) {
        try { 
            thisUDPServer = new GameServerUDP(serverPort); 
        }
        catch (IOException e) { 
            e.printStackTrace(); 
        }
    }

    public static void main(String[] args) {
        if(args.length > 1) {
            NetworkingServer app = new NetworkingServer(Integer.parseInt(args[0]), args[1]);
        }
    }
}
