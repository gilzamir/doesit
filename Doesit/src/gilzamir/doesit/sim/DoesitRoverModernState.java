package gilzamir.doesit.sim;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.collision.shapes.GImpactCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Matrix3f;
import com.jme3.math.Matrix4f;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DoesitRoverModernState extends AbstractAppState implements PhysicsTickListener {
    private Node node;
    private VehicleControl roverControl;
    private CollisionShape roverShape;
    private float mass;
    private float stiffness = 60.0f, compValue = 0.4f, dumpValue = 0.5f;
    private float maxSuspensionForce = 10000.0f;
    private Vector3f wheelDirection = new Vector3f(0,-1,0);
    private Vector3f wheelAxle = new Vector3f(-1,0,0);
    private Node wheelNode1, wheelNode2, wheelNode3, wheelNode4;
    private List<String> moduleNames = new ArrayList<String>();
    private float wheelRadius = 0.5f, yOff = 0.9f, xOff = 3.0f, zOff = 3.6f, restLenght = 0.4f;
    
    private final int maxGrabbed = 50;
    private List<RigidBodyControl> grabbedList = new ArrayList<RigidBodyControl>(maxGrabbed);

    private BulletAppState physics;
    private Vector3f roverPosition;
    private ColorRGBA colorResult = new ColorRGBA();
    private SimulationApp simulation = null;
    private RoverBatteryProfile battery;
    private Node model;
    private Map<String, AbstractModule> modules; 
    private EnginePowerProfile enginePowerProfile;
    
    public DoesitRoverModernState(float chassiMass, float initialEnergy) {
        this.mass = chassiMass;
        this.roverPosition = new Vector3f(-25.0f, 2.5f, 10.0f);
        this.battery = new RoverBatteryProfile(15000, initialEnergy);
        this.enginePowerProfile = new EnginePowerProfile();
        this.modules = new HashMap<String, AbstractModule>();
    }
    
    public void addModule(String name, AbstractModule module){
        this.modules.put(name, module);
        this.moduleNames.add(name);
    }
    
    public AbstractModule removeModule(String name) {
        AbstractModule m = this.modules.remove(name);
        moduleNames.remove(name);
        return m;
    }
    
    public AbstractModule getModule(String name) {
        return this.modules.get(name);
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        simulation = (SimulationApp) app;
        this.physics = simulation.getBulletAppState();
        Node scene = (Node)simulation.getRootNode().getChild("Scene");
        setupGeometry(simulation, scene);
        setupPhysics(physics);

        for (String k : moduleNames) {
            modules.get(k).setup(model);
        }
    }

    public SimulationApp getSimulation() {
        return simulation;
    }

    public void setRoverPosition(float x, float y, float z) {
        this.roverPosition.x = x;
        this.roverPosition.y = y;
        this.roverPosition.z = z;
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
        
        wheelNode1 = (Node)model.getChild("wheel1");
        wheelNode2 = (Node)model.getChild("wheel2");        
        wheelNode3 = (Node)model.getChild("wheel3");
        wheelNode4 = (Node)model.getChild("wheel4");
        
        wheelRadius = wheelNode1.getLocalScale().getY();
        
        rootNode.attachChild(wheelNode1);
        rootNode.attachChild(wheelNode2);
        rootNode.attachChild(wheelNode3);
        rootNode.attachChild(wheelNode4);
        
        node.move(this.roverPosition);
        rootNode.attachChild(node);        
        node.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
    }

    /**
     * Initialize physical configuration.
     * @param physicState 
     */
    public void setupPhysics(BulletAppState physicState) {
        
        CompoundCollisionShape chassiShape =  new CompoundCollisionShape();
        Geometry chassi1 = (Geometry)node.getChild("chassi1");
        Geometry chassi2 = (Geometry)node.getChild("chassi2");
        Geometry chassi3 = (Geometry)node.getChild("chassi3");
        Geometry chassi4 = (Geometry)node.getChild("chassi4");
        
        GImpactCollisionShape chassi1ColShape = new GImpactCollisionShape(chassi1.getMesh());
        GImpactCollisionShape chassi2ColShape = new GImpactCollisionShape(chassi2.getMesh());
        GImpactCollisionShape chassi3ColShape = new GImpactCollisionShape(chassi3.getMesh());
        GImpactCollisionShape chassi4ColShape = new GImpactCollisionShape(chassi4.getMesh());
        
        chassiShape.addChildShape(chassi1ColShape, chassi1.getLocalTranslation());
        chassiShape.addChildShape(chassi2ColShape, chassi2.getLocalTranslation());
        chassiShape.addChildShape(chassi3ColShape, chassi3.getLocalTranslation());  
        chassiShape.addChildShape(chassi4ColShape, chassi4.getLocalTranslation());
        
        roverShape = chassiShape;
        
        physics = physicState;
        roverControl = new VehicleControl(roverShape, mass);
        node.addControl(roverControl);
        roverControl.setSuspensionCompression(compValue);
        roverControl.setSuspensionDamping(dumpValue);
        roverControl.setSuspensionStiffness(stiffness);
        roverControl.setMaxSuspensionForce(maxSuspensionForce);
        
        
        roverControl.addWheel(wheelNode1, new Vector3f(-xOff, yOff, zOff), 
                wheelDirection, wheelAxle, restLenght, wheelRadius, true);
        roverControl.addWheel(wheelNode2, new Vector3f(xOff, yOff, zOff), 
                wheelDirection, wheelAxle, restLenght, wheelRadius, true);
        roverControl.addWheel(wheelNode3, new Vector3f(-xOff, yOff, -zOff), 
                wheelDirection, wheelAxle, restLenght, wheelRadius, false);
        roverControl.addWheel(wheelNode4, new Vector3f(xOff, yOff, -zOff), 
                wheelDirection, wheelAxle, restLenght, wheelRadius, false);

        physics.getPhysicsSpace().add(roverControl);
        physics.getPhysicsSpace().addTickListener(this);
    }

    public BulletAppState getPhysics() {
        return physics;
    }

    public float getPower() {
        return this.battery.getPower();
    }
    
    /**
     * It  returns root node of rover model.
     * @return 
     */
    public Node getNode() {
        return node;
    }

    public void brake(float force) {
        if (reserveEnergyToBrake()) {
            roverControl.brake(force);
        }
    }

    public void accelerate(float value) {
        if (reserveEnergyToAcceleration()) {
            roverControl.accelerate(value);
        }
    }
    
    public void steer(float value) {
        if (reserveEnergyToSteer()) {
            roverControl.steer(value);
        }
    }
    
    public VehicleControl getControl() {
        return roverControl;
    }

    @Override
    public void update(float fps) {
        if (simulation != null) {
            battery.storeEnergy(getReceivedEnergy() * fps);
        }
        if (reserveEnergyToLive()) {
           
            for (String k: moduleNames) {
                modules.get(k).update();
            }
            
            if (!battery.hasPowerTo(3.0f)) {
                accelerate(0);
            }
            
            if (!battery.hasPowerTo(3.0f)) {
                steer(0);
            }
        }        
    }
    
    private Vector3f tmpPos = new Vector3f(0,0,0), tmpNorm = new Vector3f(0,0,0);
    private Vector3f storePos = new Vector3f(0,0,0), storeNorm = new Vector3f(0,0,0);
    
    private float getReceivedEnergy() {
        Transform transform = node.getWorldTransform();
        
        float e = 0.0f;
        Geometry g = (Geometry)node.getChild("chassi2");
        
        FloatBuffer posBuf = g.getMesh().getFloatBuffer(VertexBuffer.Type.Position);
        FloatBuffer norBuf = g.getMesh().getFloatBuffer(VertexBuffer.Type.Normal);
        
        posBuf.rewind();
        norBuf.rewind();

        Matrix3f rotMatrix = transform.getRotation().toRotationMatrix();
        Matrix4f mmodel = new Matrix4f();
        mmodel.setTransform(transform.getTranslation(), transform.getScale(), rotMatrix);
        
        mmodel.invertLocal().transposeLocal();
        
        while (posBuf.hasRemaining()) {
            float px = posBuf.get();
            float py = posBuf.get();
            float pz = posBuf.get();
        
            float nx = norBuf.get();
            float ny = norBuf.get();
            float nz = norBuf.get();
            tmpPos.x = px;
            tmpPos.y = py;
            tmpPos.z = pz;
            tmpNorm.x = nx;
            tmpNorm.y = ny;
            tmpNorm.z = nz;
            
            transform.transformVector(tmpPos, storePos);
            storeNorm = mmodel.mult(tmpNorm);
            colorResult.r = 0.0f;
            colorResult.g = 0.0f;
            colorResult.b = 0.0f;
            
            simulation.getReflectionColor(g.getMaterial(), storePos, storeNorm, colorResult);
            e += colorResult.b;
        }
        System.out.println(e);
        return e * enginePowerProfile.solarPanelEfficiency;
    }
        
    public void prePhysicsTick(PhysicsSpace space, float tpf) {
        
    }

    public void physicsTick(PhysicsSpace space, float tpf) {
    }
    
    protected boolean reserveEnergyToAcceleration() {
        return (battery.reservePower(enginePowerProfile.accelerationPowerConsumption, false) >  0.0f);
    }

    protected boolean reserveEnergyToBrake() {
        return (battery.reservePower(enginePowerProfile.brakePowerConsumption, false) >  0.0f);
    }

    protected boolean reserveEnergyToSteer() {
        return (battery.reservePower(enginePowerProfile.steerPowerConsumption, false) >  0.0f);
    }
    
    protected boolean reserveEnergyToLive() {
        final float neccessary = 0.2f;
        return (battery.reservePower(neccessary, false) > 0.0f);

    }

    public RoverBatteryProfile getBattery() {
        return battery;
    }

    public EnginePowerProfile getEnginePowerProfile() {
        return enginePowerProfile;
    }
}
