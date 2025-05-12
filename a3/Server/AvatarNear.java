package a3.Server;

import tage.ai.behaviortrees.BTCondition;


public class AvatarNear extends BTCondition{
    NPC npc;
    NPCcontroller npcc;
    GameServerUDP server;
    
    public AvatarNear(GameServerUDP s, NPCcontroller c, NPC n, boolean toNegate) {
        super(toNegate);
        server = s; npcc = c; npc = n;
    }

    protected boolean check() {
        server.sendCheckForAvatarNear();
        boolean wasNear = npcc.getNearFlag();
        npcc.setNearFlag(false);
        return wasNear;
    }
}
