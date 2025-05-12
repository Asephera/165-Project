package a3.Server;

import tage.ai.behaviortrees.*;
import java.util.*;

public class NPCcontroller {
    private NPC npc;
    Random rn = new Random();
    BehaviorTree bt = new BehaviorTree(BTCompositeType.SELECTOR);
    boolean nearFlag = false;
    long thinkStartTime, tickStartTime;
    long lastThinkUpdateTime, lastTickUpdateTIme;
    GameServerUDP server;
    double criteria = 2.0;

    public void updateNPCs() { npc.updateLocation(); }

    public void start(GameServerUDP s) {
        thinkStartTime = System.nanoTime();
        tickStartTime = System.nanoTime();
        lastThinkUpdateTime = thinkStartTime;
        lastTickUpdateTIme = tickStartTime;
        server = s;
        setupNPCs();
        setupBehaviorTree();
        npcLoop();
    }

    public void setupNPCs() {
        npc = new NPC();
        npc.randomizeLocation(1, 2);
    }

    public void npcLoop() {
        while (true) {
            long currentTime = System.nanoTime();
            float elapsedThinkMilliSecs = (currentTime-lastThinkUpdateTime)/(1000000.0f);
            float elapsedTickMilliSecs = (currentTime-lastTickUpdateTIme)/(1000000.0f);

            if(elapsedTickMilliSecs >= 25.0f) {
                System.out.println("Tick!");
                lastTickUpdateTIme = currentTime;
                npc.updateLocation();
                server.sendNPCInfo();
            }

            if(elapsedThinkMilliSecs >= 250.0f) {
                System.out.println("think!");
                lastThinkUpdateTime = currentTime;
                bt.update(elapsedThinkMilliSecs);
            }
            Thread.yield();
        }
    }

    public void setupBehaviorTree() {
        bt.insertAtRoot(new BTSequence(10));
        bt.insertAtRoot(new BTSequence(20));
        bt.insert(10, new OneSecPassed(this,npc,false));
        bt.insert(10, new test(server, this, npc, false));
        bt.insert(20, new AvatarNear(server, this, npc, false));
        //bt.insert(20, new );
    }

    public void setNearFlag(boolean x) { nearFlag = x; }
    public boolean getNearFlag() { return nearFlag; }
    public NPC getNPC() { return npc; }
    public float getCriteria() { return 5f; }
}
