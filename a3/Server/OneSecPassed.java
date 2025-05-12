package a3.Server;

import tage.ai.behaviortrees.BTCondition;

public class OneSecPassed extends BTCondition{
    NPC npc;
    NPCcontroller npcc;
    GameServerUDP server;
    boolean toNegate;
    long last;
    
    public OneSecPassed(NPCcontroller c, NPC n, boolean toNegate) {
        super(toNegate);
        this.npcc = c; this.npc = n; this.toNegate = toNegate; this.last = System.nanoTime();
    }

    @Override
    protected boolean check() {
        long cur = System.nanoTime();
        float elapMili = (cur - last) / 1_000_000.0f;

        if(elapMili >= 1000f) {
            System.out.println("A SECOND HAS PASSED IN AFRICA");
            last = cur;
            return true;
        }
        return false;
    }
}
