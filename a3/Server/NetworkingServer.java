package a3.Server;

import java.io.IOException;

public class NetworkingServer {
    private GameServerUDP thisUDPServer;
    private NPCcontroller npcCtrl;

    public NetworkingServer(int serverPort, String protocol) {
        npcCtrl = new NPCcontroller();

        try { thisUDPServer = new GameServerUDP(serverPort, npcCtrl); }
        catch (IOException e) { System.out.println("Server did not start"); e.printStackTrace(); }

        npcCtrl.start(thisUDPServer);
    }

    public static void main(String[] args) {
        if(args.length > 1) {
            NetworkingServer app = new NetworkingServer(Integer.parseInt(args[0]), args[1]);
        }
    }
}
