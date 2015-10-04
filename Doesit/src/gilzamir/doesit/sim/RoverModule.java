package gilzamir.doesit.sim;

import java.util.HashMap;
import java.util.Map;

public abstract class RoverModule {
    protected DoesitRoverModernState rover;
    private Map<String, Float> floatProperty;
    
    
    public RoverModule(DoesitRoverModernState vehicleState) {
        this.rover = vehicleState;
        this.floatProperty = new HashMap<String, Float>();
    }
    
    public Float getFloat(String property) {
        return floatProperty.get(property);
    }
    
    public void setFloat(String property, Float value){
        this.floatProperty.put(property, value);
    }
    
    public abstract void setup();
    public abstract void exec();
}
