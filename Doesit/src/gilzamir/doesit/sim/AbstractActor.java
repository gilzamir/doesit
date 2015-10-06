package gilzamir.doesit.sim;

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.scene.Node;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractActor extends AbstractAppState {
    private Map<Class, AbstractModule> modules; 
    private List<Class> moduleType;
    protected Node node;
    protected BulletAppState physics;
    protected SimpleApplication application;
    
    public AbstractActor (SimpleApplication app) {
         this.modules = new HashMap<Class, AbstractModule>();
         this.moduleType = new ArrayList<Class>();
         this.application = app;
    }

    public void setApplication(SimpleApplication application) {
        this.application = application;
    }

    public SimpleApplication getApplication() {
        return application;
    }
    
    public void addModule(Class type, AbstractModule module) throws UnsatisfiedDependencyException {
        checkDependency(module);
        this.modules.put(type, module);
        this.moduleType.add(type);
    }
    
    public AbstractModule removeModule(Class type) {
        AbstractModule m = this.modules.remove(type);
        moduleType.remove(type);
        return m;
    }
    
    public <T extends AbstractModule> T  getModule(Class<T> type) {
        return (T)this.modules.get(type);
    }

    public List<Class> getModuleType() {
        return moduleType;
    }
    
    private void checkDependency(AbstractModule module) throws UnsatisfiedDependencyException {
        for (Class type : module.dependency) {
            if (!modules.containsKey(type)) {
                throw new UnsatisfiedDependencyException(module.getClass(), type);
            }
        }
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public BulletAppState getPhysics() {
        return physics;
    }
}
