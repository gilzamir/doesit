package gilzamir.doesit.sim;

import com.jme3.scene.Node;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractModule extends AbstractComponent {
    protected DoesitRoverModernState rover;
    protected Map<String, ModuleAction> actions;
    protected ModulePowerProfile powerProfile;
    
    public AbstractModule(DoesitRoverModernState vehicleState) {
        this.rover = vehicleState;
        this.actions = new HashMap<String, ModuleAction>();
        this.powerProfile = new ModulePowerProfile();
    }
    
    public void addAction(String name, ModuleAction action){
        actions.put(name, action);
    }

    public ModulePowerProfile getPowerProfile() {
        return powerProfile;
    }
    
    public ModuleAction getAction(String name) {
        return actions.get(name);
    }
    
    public ModuleAction removeAction(String name) {
        return this.actions.remove(name);
    }
   
    public abstract void setup(Node model);
    public abstract void act(String actionName);
    public void update() {
        //TODO
    }

}
