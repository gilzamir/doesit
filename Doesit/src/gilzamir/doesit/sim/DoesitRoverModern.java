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
import com.jme3.bullet.control.KinematicRagdollControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.CameraControl;
import java.util.ArrayList;

public class DoesitRoverModern implements RagdollCollisionListener, PhysicsTickListener {
 
    private Node roverNode;
    private VehicleControl roverControl;
    private CollisionShape roverShape;
    private float mass;
    private float stiffness = 60.0f, compValue = 0.2f, dumpValue = 0.3f;
    private float maxSuspensionForce = 10000.0f;
    private Vector3f wheelDirection = new Vector3f(0,-1,0);
    private Vector3f wheelAxle = new Vector3f(-1,0,0);
    private Node wheelNode1, wheelNode2, wheelNode3, wheelNode4;
    private float wheelRadius = 0.5f, yOff = 0.3f, xOff = 3.3f, zOff = 3.3f, restLenght = 2f;
    private Node armModelNode, roverCamModelNode;
    private boolean grabberOpened = false;
    private final int maxGrabbed = 3;
    private ArrayList<RigidBodyControl> grabbedList = new ArrayList<RigidBodyControl>(maxGrabbed);
    private RigidBodyControl grabbedObject = null, lastTouchedObject = null;
    private CameraNode camNode;
    private Camera cam;
    private ViewPort viewPort;
    private Vector3f armRootBoneRotation = new Vector3f(0,0,0);
    private Vector3f armArmBoneRotation = new Vector3f(0,0,0);
    private KinematicRagdollControl armRagDoll, camRagDoll;
    private AnimControl armAnimControl, camAnimControl;
    private AnimChannel armAnimChannel;
    private BulletAppState physics;

    
    public DoesitRoverModern(float chassiMass) {
        this.mass = chassiMass;
    }

    /**
     * It makes rover vehicle geometry and it adds code to node <code>rootNode</code>. 
     * @param app Application data.
     * @param rootNode  Rover node to geometry encapsulation.
     */
    public void initGeometry(SimpleApplication app, Node rootNode) {
       
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
        arm.move(-2, -3.0f, -1f);
       
        armModelNode.attachChild(arm);
        roverNode.attachChild(armModelNode);
       
        roverCamModelNode = new Node("roverCamNode");
        Node roverCam = (Node)model.getChild("CamArmature");
        roverCamModelNode.attachChild(roverCam);
        roverNode.attachChild(roverCamModelNode);
        roverCamModelNode.move(-2.5f, -2, 2);
        
        roverNode.move(-25,1,10);
        rootNode.attachChild(roverNode);        
        configureCamera(app, rootNode);
    }
    
    private void configureCamera(SimpleApplication app, Node rootNode) {
        cam = app.getCamera().clone();
        cam.setViewPort(0f, 0.2f, 0, 0.2f);
        
        viewPort = app.getRenderManager()
                .createMainView("Bottom Left", cam);
        viewPort.setClearFlags(true, true, true);
        viewPort.setBackgroundColor(app.getViewPort().getBackgroundColor());
        viewPort.attachScene(rootNode);
        
        camNode = new CameraNode("CamNode", cam);
        camNode.setControlDir(CameraControl.ControlDirection.SpatialToCamera);
        camNode.setLocalTranslation(new Vector3f(1.5f, 6, -0.2f));
        Quaternion quat = new Quaternion();
        quat.lookAt(Vector3f.UNIT_Z, Vector3f.UNIT_Y);
        camNode.setLocalRotation(quat);
        roverCamModelNode.attachChild(camNode);
    }

    /**
     * Initialize physical configuration.
     * @param physicState 
     */
    public void initPhysics(BulletAppState physicState) {
        
        roverShape = CollisionShapeFactory.createDynamicMeshShape(roverNode);
        
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
        camRagDoll = new KinematicRagdollControl(0);
        camRagDoll.setEventDispatchImpulseThreshold(0);
        camRagDoll.setEnabled(true);
        camRagDoll.setRootMass(1.0f);
        
        roverCam.addControl(camRagDoll);
        
        physics.getPhysicsSpace().add(camRagDoll);
        physics.getPhysicsSpace().add(armRagDoll);
        physics.getPhysicsSpace().add(roverControl);
        armRagDoll.addCollisionListener(this);
        
        physics.getPhysicsSpace().addTickListener(this);
        
    }

    public void armTurnLeft() { 
        String bone = "root";
         float MIN = -FastMath.DEG_TO_RAD * 160;
        
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

    public void armTurnRight() { 
        float MAX = FastMath.DEG_TO_RAD * 160;
        
        float lmax = FastMath.DEG_TO_RAD * 25, lmin = -FastMath.DEG_TO_RAD * 10;
        if (armArmBoneRotation.x > lmax || armArmBoneRotation.x < lmin) {
            MAX = FastMath.DEG_TO_RAD * 45;
        }

        String bone = "root";
        armRootBoneRotation.z += 0.1;
        if (armRootBoneRotation.z > MAX ) {
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
    
    public void armTurnDown() { 
        float MAX = FastMath.DEG_TO_RAD * 50;
        
        float l = FastMath.DEG_TO_RAD * 48;
        if (armRootBoneRotation.z < -l  || armRootBoneRotation.z > l) {
            MAX = FastMath.DEG_TO_RAD * 2;
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
                Vector3f.UNIT_XYZ);
    }

    public void armTurnUp() { 
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
                Vector3f.UNIT_XYZ);
    }
    
    public void grabberGrab() {
            grabberOpened = true;
            Quaternion rot = new Quaternion().fromAngles(-FastMath.DEG_TO_RAD * 90,
                    0, 0);


            Vector3f trans = armModelNode.getLocalTranslation();

            armAnimControl.getSkeleton().getBone("finger").setUserControl(true);
            armAnimControl.getSkeleton().getBone("finger").setUserTransforms(
                      trans,
                      rot,
                      Vector3f.UNIT_XYZ
                    );
            grabberOpened = true;
    }
    
    public void grabberDrop() {
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
    
    /**
     * It  returns root node of rover model.
     * @return 
     */
    public Node getNode() {
        return roverNode;
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

    public void update(float fps) {
        if (grabbedObject != null) {
            Vector3f pos = armRagDoll.getBoneRigidBody("finger").getPhysicsLocation();
            pos = pos.add(new Vector3f(0,-1,0));
            grabbedObject.setPhysicsLocation(pos);
        }
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
}
