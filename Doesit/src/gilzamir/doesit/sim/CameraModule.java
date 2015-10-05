package gilzamir.doesit.sim;

import com.jme3.animation.AnimControl;
import com.jme3.animation.Bone;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.control.KinematicRagdollControl;
import com.jme3.light.SpotLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Node;
import com.jme3.scene.control.CameraControl;

public class CameraModule extends AbstractModule {

    private AnimControl camAnimControl;
    private KinematicRagdollControl camRagDoll;
    private CameraNode camNode;
    private Camera cam;
    private ViewPort viewPort;
    private Vector3f camHeadBoneRotation = new Vector3f(0, 0, 0);
    private Node roverCamModelNode;
    private Vector3f camPosition;
    private boolean lightOn = false;
    private SpotLight light;

    public CameraModule(DoesitRoverModernState vehicleState) {
        super(vehicleState);
        this.camPosition = new Vector3f(-0.7f, 0.5f, 0.5f);
    }

    @Override
    public void setup(Node model) {
        //GEOMETRY
        roverCamModelNode = new Node("roverCamNode");
        Node roverCam = (Node) model.getChild("CamArmature");
        roverCamModelNode.attachChild(roverCam);
        rover.getNode().attachChild(roverCamModelNode);
        roverCamModelNode.move(camPosition);

        this.light = new SpotLight();
        this.light.setColor(ColorRGBA.White);
        this.light.setDirection(new Vector3f(0, 0, 1));
        this.light.setPosition(roverCam.getWorldTranslation());
        light.setSpotRange(100f);                           // distance
        light.setSpotInnerAngle(15f * FastMath.DEG_TO_RAD); // inner light cone (central beam)
        light.setSpotOuterAngle(35f * FastMath.DEG_TO_RAD); // outer light cone (edge of the light)
        light.setColor(ColorRGBA.Black);         // light color
        rover.getSimulation().getSceneNode().addLight(light);


        //PHYSICS
        Node cam = (Node)((Node)roverCamModelNode.getChild("CamArmature")).getChild("cam");
        camAnimControl = cam.getControl(AnimControl.class);

        camRagDoll = new KinematicRagdollControl(-20);
        camRagDoll.setRootMass(0.02f);
        camRagDoll.setEventDispatchImpulseThreshold(0);
        camRagDoll.setRootMass(1.0f);
        camRagDoll.addBoneName("root");

        cam.addControl(camRagDoll);

        rover.getPhysics().getPhysicsSpace().add(camRagDoll);


        //VIRTUAL CAMERA
        setupCamera(rover.getSimulation());
        
        
        //CONFIGURE ACTIONS
        addAction("TurnUp", new CameraTurnUp());
        addAction("TurnDown", new CameraTurnDown());
        addAction("TurnRight", new CameraTurnRight());
        addAction("TurnLeft", new CameraTurnLeft());
        addAction("LightOn", new LightOn());
        addAction("LightOff", new LightOff());
        getPowerProfile().setActionRequiredPower("TurnUp", 2.0f);
        getPowerProfile().setActionRequiredPower("TurnDown", 2.0f);
        getPowerProfile().setActionRequiredPower("TurnRight", 2.0f);
        getPowerProfile().setActionRequiredPower("TurnLeft", 2.0f);
        getPowerProfile().setActionRequiredPower("LightOn", 5.0f);
    }

    @Override
    public void act(String actionName) {
        getAction(actionName).exec(this, rover);
    }

    @Override
    public void update() {
        
        if (lightOn) {
            act("LightOn");
            light.setPosition(cam.getLocation());               // shine from camera loc
            light.setDirection(cam.getDirection());             // shine forward from camera loc
        }
    }

    public void setCamPosition(float x, float y, float z) {
        this.camPosition.x = x;
        this.camPosition.y = y;
        this.camPosition.z = z;
    }

    public boolean isLightOn() {
        return lightOn;
    }

    public void setLightOn(boolean lightOn) {
        this.lightOn = lightOn;
        if (lightOn) {
            light.setColor(ColorRGBA.White.mult(1.7f));
        } else {
            light.setColor(ColorRGBA.Black);
        }
    }

    public void cameraTurnUp() {
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

    public void cameraTurnDown() {
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

    public void cameraTurnRight() {
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

    public void cameraTurnLeft() {
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

    private void setupCamera(SimpleApplication app) {
        Node rootNode = app.getRootNode();
        cam = app.getCamera().clone();
        cam.setViewPort(0f, 1.0f, 0, 0.5f);
        viewPort = app.getRenderManager()
                .createMainView("First Person", cam);
        viewPort.setClearFlags(true, true, true);
        viewPort.setBackgroundColor(app.getViewPort().getBackgroundColor());
        viewPort.attachScene(rootNode);

        app.getCamera().setViewPort(0.0f, 1.0f, 0.5f, 1.0f);

        camNode = new CameraNode("CamNode", cam);
        camNode.setControlDir(CameraControl.ControlDirection.SpatialToCamera);
        camNode.setLocalTranslation(new Vector3f(0.2f, 6, -0.2f));
        Quaternion quat = new Quaternion();
        quat.lookAt(Vector3f.UNIT_Z, Vector3f.UNIT_Y);
        camNode.setLocalRotation(quat);
        roverCamModelNode.attachChild(camNode);
    }
}

class LightOn implements ModuleAction {
    public void exec(AbstractModule module, DoesitRoverModernState state) {
        CameraModule cam = (CameraModule)module;
        float power = module.getPowerProfile().getActionRequiredPower("LightOn");
        if (state.getBattery().reservePower(power, false) > 0.0f) {
            cam.setLightOn(true);
        } else if (cam.isLightOn()) {
            cam.setLightOn(false);
        }
    }
    
}

class LightOff implements ModuleAction {
    public void exec(AbstractModule module, DoesitRoverModernState state) {
        CameraModule cam = (CameraModule) module;
        cam.setLightOn(false);
    }
}

class CameraTurnLeft implements ModuleAction {
    public void exec(AbstractModule module, DoesitRoverModernState state) {
        CameraModule cam = (CameraModule) module;
        float power = cam.getPowerProfile().getActionRequiredPower("TurnLeft");
        if (state.getBattery().reservePower(power, false) > 0.0f) {
            cam.cameraTurnLeft();
        }
    }
}

class CameraTurnRight implements ModuleAction {
    public void exec(AbstractModule module, DoesitRoverModernState state) {
        CameraModule cam = (CameraModule) module;
        float power = cam.getPowerProfile().getActionRequiredPower("TurnRight");
        if (state.getBattery().reservePower(power, false) > 0.0f) {
            cam.cameraTurnRight();
        }
    }
}

class CameraTurnDown implements ModuleAction {
    public void exec(AbstractModule module, DoesitRoverModernState state) {
        CameraModule cam = (CameraModule) module;
        float power = cam.getPowerProfile().getActionRequiredPower("TurnDown");
        if (state.getBattery().reservePower(power, false) > 0.0f) {
            cam.cameraTurnDown();
        }
    }
}

class CameraTurnUp implements ModuleAction {
    public void exec(AbstractModule module, DoesitRoverModernState state) {
        CameraModule cam = (CameraModule) module;
        float power = cam.getPowerProfile().getActionRequiredPower("TurnUp");
        if (state.getBattery().reservePower(power, false) > 0.0f) {
            cam.cameraTurnUp();
        }
    }
}
