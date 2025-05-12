package a3.Server;

import tage.ai.behaviortrees.*;

public class test extends BTAction{
    NPC npc;
    NPCcontroller npcc;
    GameServerUDP server;
    
    public test(GameServerUDP s, NPCcontroller c, NPC n, boolean toNegate) {
        //super(toNegate);
        server = s; npcc = c; npc = n;
    }

    protected boolean check() {
        System.out.println("test complete");
        
        return true;
    }

    @Override
    protected BTStatus update(float f) {
        System.out.println("test complete");
        return BTStatus.BH_SUCCESS;
    }
}
