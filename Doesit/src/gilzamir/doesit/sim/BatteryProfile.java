package gilzamir.doesit.sim;

public class BatteryProfile {
    private final float capacity;
    private float power;
   
    public BatteryProfile(float capacity, float currentPower) {
        this.capacity = capacity;
        this.power = currentPower;
    }
    
    public void storeEnergy(float v) {
        power += v;
        if (power >  capacity) {
            power = capacity;
        }
    }
    
    public float reservePower(float v, boolean allowPartialReserve) {
        if (v <= power) {
            power -= v;
            return v;
        } else {
            if (allowPartialReserve){ 
                float reserved = power;
                power = 0;
                return reserved;
            } else {
                return 0.0f;
            }
        }
    }

    public float getPower() {
        return power;
    }

    public float getCapacity() {
        return capacity;
    }
    
    public boolean hasPowerTo(float p) {
        return power >= p;
    }
}
