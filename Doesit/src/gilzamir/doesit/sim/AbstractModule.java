package gilzamir.doesit.sim;

import com.jme3.scene.Node;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class AbstractModule extends AbstractComponent {
    protected AbstractActor actor;
    protected Map<String, ModuleAction> actions;
    protected ModulePowerProfile powerProfile;
    protected List<Class> dependency;
    
    public AbstractModule(AbstractActor actor) {
        this.actor = actor;
        this.actions = new HashMap<String, ModuleAction>();
        this.powerProfile = new ModulePowerProfile();
        this.dependency = new LinkedList<Class>();
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
    public List<Class> getDependency() {
        return dependency;
    }
    
    public void update(float fps) {
        //TODO
    }
}
