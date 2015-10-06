package gilzamir.doesit.sim;

import com.jme3.bullet.BulletAppState;
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
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class CarModule extends AbstractModule {
    private VehicleControl roverControl;
    private CollisionShape roverShape;
    private float mass;
    private float stiffness = 60.0f, compValue = 0.4f, dumpValue = 0.5f;
    private float maxSuspensionForce = 10000.0f;
    private Vector3f wheelDirection = new Vector3f(0,-1,0);
    private Vector3f wheelAxle = new Vector3f(-1,0,0);
    private Node wheelNode1, wheelNode2, wheelNode3, wheelNode4;
    private Vector3f roverPosition;
    private BatteryProfile battery;
    
    private float wheelRadius = 0.5f, yOff = 0.9f, xOff = 3.0f, zOff = 3.6f, restLenght = 0.4f;
    
    private final int maxGrabbed = 50;
    private List<RigidBodyControl> grabbedList = new ArrayList<RigidBodyControl>(maxGrabbed);
    private EnginePowerProfile enginePowerProfile;
    
    
    public CarModule(DoesitRoverModern rover, float chassiMass) {
        super(rover);
        this.mass = chassiMass;
        this.enginePowerProfile = new EnginePowerProfile();
        this.roverPosition = new Vector3f(-25.0f, 2.5f, 10.0f);
        this.battery = new BatteryProfile(15000, 15000);
    }
    
    public void setRoverPosition(float x, float y, float z) {
        this.roverPosition.x = x;
        this.roverPosition.y = y;
        this.roverPosition.z = z;
    }
    
    @Override
    public void setup(Node model) {
        //CONFIGURE GEOMETRY
        wheelNode1 = (Node)model.getChild("wheel1");
        wheelNode2 = (Node)model.getChild("wheel2");        
        wheelNode3 = (Node)model.getChild("wheel3");
        wheelNode4 = (Node)model.getChild("wheel4");
        
        wheelRadius = wheelNode1.getLocalScale().getY();
        actor.getNode().attachChild(wheelNode1);
        actor.getNode().attachChild(wheelNode2);
        actor.getNode().attachChild(wheelNode3);
        actor.getNode().attachChild(wheelNode4);
        
        actor.getNode().move(this.roverPosition);
    
        //CONFIGURE PHYSICS
        CompoundCollisionShape chassiShape =  new CompoundCollisionShape();
        Geometry chassi1 = (Geometry)actor.getNode().getChild("chassi1");
        Geometry chassi2 = (Geometry)actor.getNode().getChild("chassi2");
        Geometry chassi3 = (Geometry)actor.getNode().getChild("chassi3");
        Geometry chassi4 = (Geometry)actor.getNode().getChild("chassi4");
        
        GImpactCollisionShape chassi1ColShape = new GImpactCollisionShape(chassi1.getMesh());
        GImpactCollisionShape chassi2ColShape = new GImpactCollisionShape(chassi2.getMesh());
        GImpactCollisionShape chassi3ColShape = new GImpactCollisionShape(chassi3.getMesh());
        GImpactCollisionShape chassi4ColShape = new GImpactCollisionShape(chassi4.getMesh());
        
        chassiShape.addChildShape(chassi1ColShape, chassi1.getLocalTranslation());
        chassiShape.addChildShape(chassi2ColShape, chassi2.getLocalTranslation());
        chassiShape.addChildShape(chassi3ColShape, chassi3.getLocalTranslation());  
        chassiShape.addChildShape(chassi4ColShape, chassi4.getLocalTranslation());
        
        roverShape = chassiShape;
        
        BulletAppState physics = actor.getPhysics();
        roverControl = new VehicleControl(roverShape, mass);
        actor.getNode().addControl(roverControl);
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
        
        //CONFIGURE ACTIONS
        
    }

    @Override
    public void act(String actionName) {
    }

    public BatteryProfile getBattery() {
        return battery;
    }

    
    @Override
    public void update(float fps) {
        super.update(fps);
        if (actor.physics != null) {
            battery.storeEnergy(getReceivedEnergy() * fps);
        }
        if (reserveEnergyToLive()) {
            if (!battery.hasPowerTo(3.0f)) {
                accelerate(0);
            }
            
            if (!battery.hasPowerTo(3.0f)) {
                steer(0);
            }
        }    
    }
    
    public void brake(float force) {
        roverControl.brake(force);   
    }

    public void accelerate(float value) {
        roverControl.accelerate(value);
    }
    
    public void steer(float value) {
        roverControl.steer(value);
    }
    
    public VehicleControl getControl() {
        return roverControl;
    }
    
    private Vector3f tmpPos = new Vector3f(0,0,0), tmpNorm = new Vector3f(0,0,0);
    private Vector3f storePos = new Vector3f(0,0,0), storeNorm = new Vector3f(0,0,0);
    private ColorRGBA colorResult = new ColorRGBA();
    private float getReceivedEnergy() {
        Transform transform = actor.getNode().getWorldTransform();
        
        float e = 0.0f;
        Geometry g = (Geometry)actor.getNode().getChild("chassi2");
        
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
            WorldTimeState timeState = (WorldTimeState) getObject("WorldTime");
            timeState.getReflectionColor(g.getMaterial(), storePos, storeNorm, colorResult, 0);
            e += colorResult.b;
        }
        System.out.println(e);
        return e * enginePowerProfile.solarPanelEfficiency;
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

    public EnginePowerProfile getEnginePowerProfile() {
        return enginePowerProfile;
    }
}
