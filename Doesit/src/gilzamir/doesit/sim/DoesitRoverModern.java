package gilzamir.doesit.sim;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.Bone;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.RagdollCollisionListener;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.collision.shapes.GImpactCollisionShape;
import com.jme3.bullet.control.KinematicRagdollControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.control.CameraControl;
import java.nio.FloatBuffer;
import java.util.ArrayList;

public class DoesitRoverModern implements RagdollCollisionListener, PhysicsTickListener {
 
    private Node roverNode;
    private VehicleControl roverControl;
    private CollisionShape roverShape;
    private float mass;
    private float stiffness = 60.0f, compValue = 0.4f, dumpValue = 0.5f;
    private float maxSuspensionForce = 10000.0f;
    private Vector3f wheelDirection = new Vector3f(0,-1,0);
    private Vector3f wheelAxle = new Vector3f(-1,0,0);
    private Node wheelNode1, wheelNode2, wheelNode3, wheelNode4;
    private float wheelRadius = 0.5f, yOff = 0.9f, xOff = 3.0f, zOff = 3.6f, restLenght = 0.4f;
    private Node armModelNode, roverCamModelNode;
    private boolean grabberOpened = false;
    private final int maxGrabbed = 3;
    private ArrayList<RigidBodyControl> grabbedList = new ArrayList<RigidBodyControl>(maxGrabbed);
    private RigidBodyControl grabbedObject = null;
    private CameraNode camNode;
    private Camera cam;
    private ViewPort viewPort;
    private Vector3f armRootBoneRotation = new Vector3f(0,0,0);
    private Vector3f armArmBoneRotation = new Vector3f(0,0,0);
    private Vector3f armScale = new Vector3f(1,1,1);
    private Vector3f camHeadBoneRotation = new Vector3f(0,0,0);
    private KinematicRagdollControl armRagDoll, camRagDoll;
    private AnimControl armAnimControl, camAnimControl;
    private AnimChannel armAnimChannel;
    private BulletAppState physics;

    private Vector3f armPosition;
    private Vector3f camPosition;
    private Vector3f roverPosition;
    
    private Vector3f colorResult = new Vector3f(0,0,0);
    private SimulationApp simulation = null;
    
    private float energy;
    private float energyEfficiency;
    
    public DoesitRoverModern(float chassiMass, float initialEnergy) {
        this.mass = chassiMass;
        this.armPosition = new Vector3f(-0.7f, 1.0f, -1.6f);
        this.camPosition = new Vector3f(-0.7f, 0.5f, 0.5f);
        this.roverPosition = new Vector3f(-25.0f, 2.5f, 10.0f);
        this.energy = initialEnergy;
        this.energyEfficiency = 1.0f;
    }

    public void setArmPosition(float x, float y, float z) {
        this.armPosition.x = x;
        this.armPosition.y = y;
        this.armPosition.z = z;
    }
    
    public void setCamPosition(float x, float  y, float z) {
        this.camPosition.x = x;
        this.camPosition.y = y;
        this.camPosition.z = z;
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
        
        Node model = (Node)app.getAssetManager().loadModel("/Models/doesithover/car.j3o");
        roverNode = (Node) model.getChild("chassi");
        

        wheelNode1 = (Node)model.getChild("wheel1");
        wheelNode2 = (Node)model.getChild("wheel2");        
        wheelNode3 = (Node)model.getChild("wheel3");
        wheelNode4 = (Node)model.getChild("wheel4");
        
        wheelRadius = wheelNode1.getLocalScale().getY();
        
        rootNode.attachChild(wheelNode1);
        rootNode.attachChild(wheelNode2);
        rootNode.attachChild(wheelNode3);
        rootNode.attachChild(wheelNode4);
        
        armModelNode = new Node("armNode");
        Node arm = (Node)model.getChild("GrabberArmature");
        arm.move(armPosition);
       
        armModelNode.attachChild(arm);
        roverNode.attachChild(armModelNode);
       
        roverCamModelNode = new Node("roverCamNode");
        Node roverCam = (Node)model.getChild("CamArmature");
        roverCamModelNode.attachChild(roverCam);
        roverNode.attachChild(roverCamModelNode);
        roverCamModelNode.move(camPosition);
        
        roverNode.move(this.roverPosition);
        rootNode.attachChild(roverNode);        
        setupCamera(app, rootNode);
        roverNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
    }
    
    private void setupCamera(SimpleApplication app, Node rootNode) {
    /*    cam = app.getCamera().clone();
        cam.setViewPort(0f, 0.1f, 0, 0.1f);
        viewPort = app.getRenderManager()
                .createMainView("Bottom Left", cam);
        viewPort.setClearFlags(true, true, true);
        viewPort.setBackgroundColor(app.getViewPort().getBackgroundColor());
        viewPort.attachScene(rootNode);
        
        app.getCamera().setViewPort(0.0f, 1.0f, 0.0f, 1.0f);
        
        
        camNode = new CameraNode("CamNode", cam);
        camNode.setControlDir(CameraControl.ControlDirection.SpatialToCamera);
        camNode.setLocalTranslation(new Vector3f(0.2f, 6, -0.2f));
        Quaternion quat = new Quaternion();
        quat.lookAt(Vector3f.UNIT_Z, Vector3f.UNIT_Y);
        camNode.setLocalRotation(quat);
        roverCamModelNode.attachChild(camNode);*/
    }

    /**
     * Initialize physical configuration.
     * @param physicState 
     */
    public void setupPhysics(BulletAppState physicState) {
        
        CompoundCollisionShape chassiShape =  new CompoundCollisionShape();
        Geometry chassi1 = (Geometry)roverNode.getChild("chassi1");
        Geometry chassi2 = (Geometry)roverNode.getChild("chassi2");
        Geometry chassi3 = (Geometry)roverNode.getChild("chassi3");
        Geometry chassi4 = (Geometry)roverNode.getChild("chassi4");
        
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
        roverNode.addControl(roverControl);
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

        
        Node armature = (Node) ((Node)armModelNode.getChild("GrabberArmature")).getChild("arm");
        
        armAnimControl = armature.getControl(AnimControl.class);
        armAnimChannel = armAnimControl.createChannel();
        armAnimChannel.setAnim("idletop");
 
        armRagDoll = new KinematicRagdollControl(-20f);
        armRagDoll.setRootMass(0.02f);
        armRagDoll.setEventDispatchImpulseThreshold(0.0f);
        armRagDoll.addCollisionListener(this);
        armature.addControl(armRagDoll);        
        armRagDoll.addBoneName("root");
 

        Node roverCam = (Node)((Node)roverCamModelNode.getChild("CamArmature")).getChild("cam");
        camAnimControl = roverCam.getControl(AnimControl.class);
  
        camRagDoll = new KinematicRagdollControl(-20);
        camRagDoll.setRootMass(0.02f);
        camRagDoll.setEventDispatchImpulseThreshold(0);
        camRagDoll.setRootMass(1.0f);
        camRagDoll.addBoneName("root");
        
        
        roverCam.addControl(camRagDoll);
        
        physics.getPhysicsSpace().add(camRagDoll);
        physics.getPhysicsSpace().add(armRagDoll);
        physics.getPhysicsSpace().add(roverControl);
        armRagDoll.addCollisionListener(this);
        
        physics.getPhysicsSpace().addTickListener(this);
        
    }

    public float getEnergy() {
        return energy;
    }
    
    public void armTurnLeft() {
        if (reserveArmMovementEnergy()) {
            String bone = "root";
            float MIN = -FastMath.DEG_TO_RAD * 165;

            float lmax = FastMath.DEG_TO_RAD * 25, lmin = -FastMath.DEG_TO_RAD * 10;
            if (armArmBoneRotation.x > lmax || armArmBoneRotation.x < lmin) {
                MIN = -FastMath.DEG_TO_RAD * 45;
            }
            armRootBoneRotation.z -= 0.1;
            if (armRootBoneRotation.z < MIN) {
                armRootBoneRotation.z = MIN;
            }

            Quaternion rot = new Quaternion().fromAngles(armRootBoneRotation.getX(),
                    armRootBoneRotation.getY(), armRootBoneRotation.getZ());


            Vector3f trans = armModelNode.getLocalTranslation();

            armAnimControl.getSkeleton().getBone(bone).setUserControl(true);

            armAnimControl.getSkeleton().getBone(bone).setUserTransforms(
                    trans,
                    rot,
                    Vector3f.UNIT_XYZ);
        }
    }

    public void armTurnRight() {
        if (reserveArmMovementEnergy()) {
            float MAX = FastMath.DEG_TO_RAD * 165;

            float lmax = FastMath.DEG_TO_RAD * 25, lmin = -FastMath.DEG_TO_RAD * 10;
            if (armArmBoneRotation.x > lmax || armArmBoneRotation.x < lmin) {
                MAX = FastMath.DEG_TO_RAD * 45;
            }

            String bone = "root";
            armRootBoneRotation.z += 0.1;
            if (armRootBoneRotation.z > MAX) {
                armRootBoneRotation.z = MAX;
            }
            Quaternion rot = new Quaternion().fromAngles(armRootBoneRotation.getX(),
                    armRootBoneRotation.getY(), armRootBoneRotation.getZ());


            Vector3f trans = armModelNode.getLocalTranslation();

            armAnimControl.getSkeleton().getBone(bone).setUserControl(true);
            armAnimControl.getSkeleton().getBone(bone).setUserTransforms(
                    trans,
                    rot,
                    Vector3f.UNIT_XYZ);
        }
    }
    
    public void armTurnDown() {
        if (reserveArmMovementEnergy()) {
            float MAX = FastMath.DEG_TO_RAD * 50;

            float l = FastMath.DEG_TO_RAD * 48;
            if (armRootBoneRotation.z < -l || armRootBoneRotation.z > l) {
                MAX = FastMath.DEG_TO_RAD * 10;
            }

            String bone = "arm";


            armArmBoneRotation.x += 0.1;
            if (armArmBoneRotation.x > MAX) {
                armArmBoneRotation.x = MAX;
            }

            Quaternion rot = new Quaternion().fromAngles(armArmBoneRotation.getX(),
                    armArmBoneRotation.getY(), armArmBoneRotation.getZ());


            Vector3f trans = armModelNode.getLocalTranslation();

            armAnimControl.getSkeleton().getBone(bone).setUserControl(true);

            armAnimControl.getSkeleton().getBone(bone).setUserTransforms(
                    trans,
                    rot,
                    armScale);
        }
    }

    public void armTurnUp() {
        if (reserveArmMovementEnergy()) {
            float MIN = -FastMath.DEG_TO_RAD * 10;


            String bone = "arm";
            armArmBoneRotation.x -= 0.1;
            if (armArmBoneRotation.x < MIN) {
                armArmBoneRotation.x = MIN;
            }
            Quaternion rot = new Quaternion().fromAngles(armArmBoneRotation.getX(),
                    armArmBoneRotation.getY(), armArmBoneRotation.getZ());


            Vector3f trans = armModelNode.getLocalTranslation();

            armAnimControl.getSkeleton().getBone(bone).setUserControl(true);
            armAnimControl.getSkeleton().getBone(bone).setUserTransforms(
                    trans,
                    rot,
                    armScale);
        }
    }
    
    public void grabberGrab() {
        if (reserveArmMovementEnergy()) {
            grabberOpened = true;
            Quaternion rot = new Quaternion().fromAngles(-FastMath.DEG_TO_RAD * 90,
                    0, 0);


            Vector3f trans = armModelNode.getLocalTranslation();

            armAnimControl.getSkeleton().getBone("finger").setUserControl(true);
            armAnimControl.getSkeleton().getBone("finger").setUserTransforms(
                    trans,
                    rot,
                    Vector3f.UNIT_XYZ);
            grabberOpened = true;
        }
    }
    
    public void grabberDrop() {
        if (reserveArmMovementEnergy()) {
            grabberOpened = false;
            grabbedObject = null;
            Quaternion rot = new Quaternion().fromAngles(0,
                    0, 0);


            Vector3f trans = armModelNode.getLocalTranslation();

            armAnimControl.getSkeleton().getBone("finger").setUserControl(true);
            armAnimControl.getSkeleton().getBone("finger").setUserTransforms(
                    trans,
                    rot,
                    Vector3f.UNIT_XYZ);
        }
    }

    
    public void cameraTurnUp() {
        if (reserveCameraMovementEnergy()) {
            float MIN = FastMath.DEG_TO_RAD * -40;

            String boneName = "head";

            Bone bone = camAnimControl.getSkeleton().getBone(boneName);
            bone.setUserControl(true);


            camHeadBoneRotation.x -= 0.1;
            if (camHeadBoneRotation.x < MIN) {
                camHeadBoneRotation.x = MIN;
            }

            Quaternion rot = new Quaternion().fromAngles(camHeadBoneRotation.x,
                    camHeadBoneRotation.y, camHeadBoneRotation.z);
            Quaternion camNodeRot = new Quaternion().fromAngles(camHeadBoneRotation.x,
                    camHeadBoneRotation.z, camHeadBoneRotation.y);

            Vector3f trans = new Vector3f(0, 0, 0);
            camNode.setLocalRotation(camNodeRot);
            bone.setUserTransforms(
                    trans,
                    rot,
                    Vector3f.UNIT_XYZ);
        }
    }
    
    public void cameraTurnDown() {
        if (reserveCameraMovementEnergy()) {
            float MAX = FastMath.DEG_TO_RAD * 30;

            String boneName = "head";

            Bone bone = camAnimControl.getSkeleton().getBone(boneName);
            bone.setUserControl(true);


            camHeadBoneRotation.x += 0.1;
            if (camHeadBoneRotation.x > MAX) {
                camHeadBoneRotation.x = MAX;
            }

            Quaternion rot = new Quaternion().fromAngles(camHeadBoneRotation.x,
                    camHeadBoneRotation.y, camHeadBoneRotation.z);
            Quaternion camNodeRot = new Quaternion().fromAngles(camHeadBoneRotation.x,
                    camHeadBoneRotation.z, camHeadBoneRotation.y);

            Vector3f trans = new Vector3f(0, 0, 0);
            camNode.setLocalRotation(camNodeRot);
            bone.setUserTransforms(
                    trans,
                    rot,
                    Vector3f.UNIT_XYZ);
        }
    }
    
    public void cameraTurnRight() {
        if (reserveCameraMovementEnergy()) {
            float MIN = FastMath.DEG_TO_RAD * -40;

            String boneName = "head";

            Bone bone = camAnimControl.getSkeleton().getBone(boneName);
            bone.setUserControl(true);


            camHeadBoneRotation.z -= 0.1;
            if (camHeadBoneRotation.z < MIN) {
                camHeadBoneRotation.z = MIN;
            }

            Quaternion rot = new Quaternion().fromAngles(camHeadBoneRotation.x,
                    camHeadBoneRotation.y, camHeadBoneRotation.z);
            Quaternion camNodeRot = new Quaternion().fromAngles(camHeadBoneRotation.x,
                    camHeadBoneRotation.z, camHeadBoneRotation.y);

            Vector3f trans = new Vector3f(0, 0, 0);
            camNode.setLocalRotation(camNodeRot);
            bone.setUserTransforms(
                    trans,
                    rot,
                    Vector3f.UNIT_XYZ);
        }
    }
    
    public void cameraTurnLeft() {
        if (reserveCameraMovementEnergy()) {
            float MAX = FastMath.DEG_TO_RAD * 30;

            String boneName = "head";

            Bone bone = camAnimControl.getSkeleton().getBone(boneName);
            bone.setUserControl(true);


            camHeadBoneRotation.z += 0.1;
            if (camHeadBoneRotation.z > MAX) {
                camHeadBoneRotation.z = MAX;
            }

            Quaternion rot = new Quaternion().fromAngles(camHeadBoneRotation.x,
                    camHeadBoneRotation.y, camHeadBoneRotation.z);

            Quaternion camNodeRot = new Quaternion().fromAngles(camHeadBoneRotation.x,
                    camHeadBoneRotation.z, camHeadBoneRotation.y);


            Vector3f trans = new Vector3f(0, 0, 0);

            camNode.setLocalRotation(camNodeRot);
            bone.setUserTransforms(
                    trans,
                    rot,
                    Vector3f.UNIT_XYZ);
        }
    }
    
    public void sketcheArm() {
        if (reserveArmMovementEnergy()) {
            Bone bone = armAnimControl.getSkeleton().getBone("arm");

            Vector3f trans = this.armModelNode.getLocalTranslation();
            Quaternion qu = this.armModelNode.getLocalRotation();

            armScale.z += 0.1;
            if (armScale.z > 1.3f) {
                armScale.z = 1.3f;
            }

            bone.setUserControl(true);
            bone.setUserTransforms(
                    trans,
                    qu,
                    armScale);
        }
    }
    
    public void shortenArm() {
        if (reserveArmMovementEnergy()) {
            Bone bone = armAnimControl.getSkeleton().getBone("arm");

            Vector3f trans = this.armModelNode.getLocalTranslation();
            Quaternion qu = this.armModelNode.getLocalRotation();

            armScale.z -= 0.1;
            if (armScale.z < 0.5f) {
                armScale.z = 0.5f;
            }

            bone.setUserControl(true);
            bone.setUserTransforms(
                    trans,
                    qu,
                    armScale);
        }
    }
    
    /**
     * It  returns root node of rover model.
     * @return 
     */
    public Node getNode() {
        return roverNode;
    }

    public void brake(float force) {
        if (reserveMovementEnergy()) {
            roverControl.brake(force);
        }
    }

    public void accelerate(float value) {
        if (reserveMovementEnergy()) {
            roverControl.accelerate(value);
        }
    }
    
    public void steer(float value) {
        if (reserveMovementEnergy()) {
            roverControl.steer(value);
        }
    }
    
    public VehicleControl getControl() {
        return roverControl;
    }

    public void update(float fps) {
        if (grabbedObject != null) {
            Vector3f pos = armRagDoll.getBoneRigidBody("finger").getPhysicsLocation();
            pos = pos.add(new Vector3f(0,-1,0));
            grabbedObject.setPhysicsLocation(pos);
        }
        if (simulation != null) {
             energy += getReceivedEnergy();
        }
    }
    
    private Vector3f tmpPos = new Vector3f(0,0,0), tmpNorm = new Vector3f(0,0,0);
    private Vector3f storePos = new Vector3f(0,0,0), storeNorm = new Vector3f(0,0,0);
    
    private float getReceivedEnergy() {
        Transform transform = roverNode.getWorldTransform();
        
        float e = 0.0f;
        Geometry g = (Geometry)roverNode.getChild("chassi2");
        
        FloatBuffer posBuf = g.getMesh().getFloatBuffer(VertexBuffer.Type.Position);
        FloatBuffer norBuf = g.getMesh().getFloatBuffer(VertexBuffer.Type.Normal);
        
        posBuf.rewind();
        norBuf.rewind();
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
            storeNorm = transform.getRotation().mult(tmpNorm);
            
            simulation.getColorOf(g.getMaterial(), storePos, storeNorm, colorResult);
            
            e += colorResult.z;
        }
        System.out.println(e);
        return e * energyEfficiency;
    }
    
    public void collide(Bone bone, PhysicsCollisionObject object, PhysicsCollisionEvent event) {
        if (bone != null) {
            if (bone.getName().equals("finger")) {
                if (object.getUserObject() != null && object.getUserObject() instanceof Spatial) {
                    Spatial sp = (Spatial) object.getUserObject();
                    if (sp.getName().endsWith("Collectable")) {
                        if (grabberOpened) {
                            grabbedObject = sp.getControl(RigidBodyControl.class);
                        }
                    }
                }
            }
        }
    }
    
    public void prePhysicsTick(PhysicsSpace space, float tpf) {
        
    }

    public void physicsTick(PhysicsSpace space, float tpf) {
    }
    
    protected boolean reserveCameraMovementEnergy() {
        final float neccessary = 2.0f;
        if (energy > neccessary) {
            energy -= neccessary;
            return true;
        } else {
            return false;
        }
    }
    

    protected boolean reserveArmMovementEnergy() {
        final float neccessary = 5.0f;
        if (energy > neccessary) {
            energy -= neccessary;
            return true;
        } else {
            return false;
        }
    }
    
    protected boolean reserveMovementEnergy() {
        final float neccessary = 3.0f;
        if (energy > neccessary) {
            energy -= neccessary;
            return true;
        } else {
            return false;
        }
    }
}
