package a3.Server;

public class NPC {
    double locationX, locationY, locationZ;
    double dir = 0.1;
    double size = 1.0;

    public NPC() {
        locationX = 0.0;
        locationY = 0.0;
        locationZ = 0.0;
    }

    public void randomizeLocation(int seedX, int seedZ) {
        locationX = ((double)seedX)/4.0-5.0;
        locationY = 0;
        locationZ = ((double)seedZ)/4.0-5.0;
    }

    public double getX() { return locationX; }
    public double getY() { return locationY; }
    public double getZ() { return locationZ; }
    public double getSize() { return size; }
    // methods and shi changing size here

    // moves back and forth
    public void updateLocation() {
        if(locationX > 10) dir=-0.1;
        if(locationX < 10) dir=0.1;
        locationX = locationX + dir;
    }
}
