package gilzamir.doesit.sim;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppStateManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;



public class DoesitRoverModern extends AbstractActor implements PhysicsTickListener {
    private SimulationApp simulation = null;
    private Node model;
    
    public DoesitRoverModern(SimpleApplication app) {
        super(app);
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        simulation = (SimulationApp) app;
        this.physics = simulation.getBulletAppState();
        
        Node scene = (Node)simulation.getRootNode().getChild("Scene");
        setupGeometry(simulation, scene);
        setupPhysics(physics);

        for (Class<AbstractModule> k : getModuleType()) {
            getModule(k).setup(model);
        }
    }

    public SimulationApp getSimulation() {
        return simulation;
    }
    
    /**
     * It makes rover vehicle geometry and it adds code to node <code>rootNode</code>. 
     * @param app Application data.
     * @param rootNode  Rover node to geometry encapsulation.
     */
    public void setupGeometry(SimpleApplication app, Node rootNode) {
       
        if (app instanceof SimulationApp) {
            simulation = (SimulationApp) app;
        }
        
        model = (Node)app.getAssetManager().loadModel("/Models/doesithover/car.j3o");
        node = (Node) model.getChild("chassi");
        
      
        rootNode.attachChild(node);        
        node.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
    }

    /**
     * Initialize physical configuration.
     * @param physicState 
     */
    public void setupPhysics(BulletAppState physicState) {
        physics.getPhysicsSpace().addTickListener(this);
    }

    public BulletAppState getPhysics() {
        return physics;
    }
    
    @Override
    public void update(float fps) {           
        for (Class<AbstractModule> k: getModuleType()) {
            getModule(k).update(fps);
        }   
    }
    
        
    public void prePhysicsTick(PhysicsSpace space, float tpf) {
        
    }

    public void physicsTick(PhysicsSpace space, float tpf) {
   
    }
    
}
