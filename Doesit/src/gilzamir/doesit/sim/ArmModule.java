package gilzamir.doesit.sim;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.Bone;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.RagdollCollisionListener;
import com.jme3.bullet.control.KinematicRagdollControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class ArmModule extends AbstractModule implements RagdollCollisionListener {

    private Vector3f armRootBoneRotation = new Vector3f(0, 0, 0);
    private Vector3f armArmBoneRotation = new Vector3f(0, 0, 0);
    private Vector3f armScale = new Vector3f(1, 1, 1);
    private boolean grabberOpened = false;
    private Vector3f armPosition;
    private AnimControl armAnimControl;
    private AnimChannel armAnimChannel;
    private Node armModelNode;
    private KinematicRagdollControl armRagDoll;
    private RigidBodyControl grabbedObject = null;

    public ArmModule(DoesitRoverModernState vehicleState) {
        super(vehicleState);
        this.armPosition = new Vector3f(-0.7f, 1.0f, -1.6f);
        this.setVector3f("ArmPosition", armPosition);
        this.setObject("GrabbedObject", grabbedObject);
        this.addAction("TurnRight", new ArmTurnRight());
        this.addAction("TurnLeft", new ArmTurnLeft());
        this.addAction("TurnUp", new ArmTurnUp());
        this.addAction("TurnDown", new ArmTurnDown());
        this.addAction("Grab", new GrabberGrab());
        this.addAction("Drop", new GrabberDrop());
        this.addAction("Shorten", new ShortenArm());
        this.addAction("Sketche", new SketcheArm());
        getPowerProfile().setActionRequiredPower("TurnRight", 3.0f);
        getPowerProfile().setActionRequiredPower("TurnLeft", 3.0f);
        getPowerProfile().setActionRequiredPower("TurnUp", 3.0f);
        getPowerProfile().setActionRequiredPower("TurnDown", 3.0f);
        getPowerProfile().setActionRequiredPower("Grab", 3.0f);
        getPowerProfile().setActionRequiredPower("Drop", 3.0f);
        getPowerProfile().setActionRequiredPower("Shorten", 3.0f);
        getPowerProfile().setActionRequiredPower("Sketche", 3.0f);
    }

    @Override
    public void setup(Node model) {
        //GEOMETRY CONFIGURATION
        armModelNode = new Node("armNode");
        Node arm = (Node) model.getChild("GrabberArmature");
        arm.move(armPosition);

        armModelNode.attachChild(arm);
        rover.getNode().attachChild(armModelNode);


        //PHYSICS CONFIGURATION
        Node armature = (Node) ((Node) armModelNode.getChild("GrabberArmature")).getChild("arm");

        armAnimControl = armature.getControl(AnimControl.class);
        armAnimChannel = armAnimControl.createChannel();
        armAnimChannel.setAnim("idletop");

        armRagDoll = new KinematicRagdollControl(-20f);
        armRagDoll.setRootMass(0.02f);
        armRagDoll.setEventDispatchImpulseThreshold(0.0f);
        armRagDoll.addCollisionListener(this);
        armature.addControl(armRagDoll);
        armRagDoll.addBoneName("root");
        rover.getPhysics().getPhysicsSpace().add(armRagDoll);
    }

    public void collide(Bone bone, PhysicsCollisionObject object, PhysicsCollisionEvent event) {
        if (bone != null) {
            if (bone.getName().equals("finger")) {
                if (object.getUserObject() != null && object.getUserObject() instanceof Spatial) {
                    Spatial sp = (Spatial) object.getUserObject();
                    if (sp.getName().endsWith("Collectable")) {
                        if (grabberOpened) {
                            this.grabbedObject = sp.getControl(RigidBodyControl.class);
                            setObject("GrabbedObject", this.grabbedObject);
                        }
                    }
                }
            }
        }
    }

    public void setArmPosition(float x, float y, float z) {
        this.armPosition.x = x;
        this.armPosition.y = y;
        this.armPosition.z = z;
    }

    @Override
    public void act(String actionName) {
        getAction(actionName).exec(this, rover);
    }

    public void armTurnLeft() {
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

    public void armTurnRight() {
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

    public void armTurnDown() {
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
                armScale);
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
                Vector3f.UNIT_XYZ);
        grabberOpened = true;
    }

    public void grabberDrop() {
        grabberOpened = false;
        grabbedObject = null;
        setObject("GrabbedObject", null);
        Quaternion rot = new Quaternion().fromAngles(0,
                0, 0);


        Vector3f trans = armModelNode.getLocalTranslation();

        armAnimControl.getSkeleton().getBone("finger").setUserControl(true);
        armAnimControl.getSkeleton().getBone("finger").setUserTransforms(
                trans,
                rot,
                Vector3f.UNIT_XYZ);
    }

    public void sketcheArm() {
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

    public void shortenArm() {
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

    public Object getGrabbedObject() {
        return grabbedObject;
    }

    public void update() {
        if (grabbedObject != null) {
            Vector3f pos = armRagDoll.getBoneRigidBody("finger").getPhysicsLocation();
            pos = pos.add(new Vector3f(0, -1, 0));
            grabbedObject.setPhysicsLocation(pos);
        }
    }
}

class ArmTurnRight implements ModuleAction {

    public void exec(AbstractModule module, DoesitRoverModernState state) {
        float power = module.getPowerProfile().getActionRequiredPower("TurnRight");
        if (state.getBattery().reservePower(power, false) > 0.0f){ 
            ((ArmModule) module).armTurnRight();
        }
    }
}

class ArmTurnLeft implements ModuleAction {

    public void exec(AbstractModule module, DoesitRoverModernState state) {
        float power = module.getPowerProfile().getActionRequiredPower("TurnLeft");
        if (state.getBattery().reservePower(power, false) > 0.0f){ 
            ((ArmModule) module).armTurnLeft();
        }
    }
}

class ArmTurnDown implements ModuleAction {

    public void exec(AbstractModule module, DoesitRoverModernState state) {
        float power = module.getPowerProfile().getActionRequiredPower("TurnDown");
        if (state.getBattery().reservePower(power, false) > 0.0f){ 
            ((ArmModule) module).armTurnDown();
        }    
    }
}

class ArmTurnUp implements ModuleAction {

    public void exec(AbstractModule module, DoesitRoverModernState state) {
        float power = module.getPowerProfile().getActionRequiredPower("TurnUp");
        if (state.getBattery().reservePower(power, false) > 0.0f){ 
            ((ArmModule) module).armTurnUp();
        }
    }
}

class SketcheArm implements ModuleAction {

    public void exec(AbstractModule module, DoesitRoverModernState state) {
        float power = module.getPowerProfile().getActionRequiredPower("Sketche");
        if (state.getBattery().reservePower(power, false) > 0.0f){ 
            ((ArmModule) module).sketcheArm();
        }
    }
}

class ShortenArm implements ModuleAction {

    public void exec(AbstractModule module, DoesitRoverModernState state) {
        ((ArmModule) module).shortenArm();
        float power = module.getPowerProfile().getActionRequiredPower("Shorten");
        if (state.getBattery().reservePower(power, false) > 0.0f){ 
            ((ArmModule) module).shortenArm();
        }
    }
}

class GrabberDrop implements ModuleAction {
    
    public void exec(AbstractModule module, DoesitRoverModernState state) {
        float power = module.getPowerProfile().getActionRequiredPower("Drop");
        if (state.getBattery().reservePower(power, false) > 0.0f){ 
            ((ArmModule) module).grabberDrop();
        }
    }
}

class GrabberGrab implements ModuleAction {

    public void exec(AbstractModule module, DoesitRoverModernState state) {
        float power = module.getPowerProfile().getActionRequiredPower("Grab");
        if (state.getBattery().reservePower(power, false) > 0.0f){ 
            ((ArmModule) module).grabberGrab();
        }
    }
}
