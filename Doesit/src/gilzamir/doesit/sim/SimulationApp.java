package gilzamir.doesit.sim;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;

public class SimulationApp extends SimpleApplication implements ActionListener {

    private Node sceneNode;
    private BulletAppState bulletAppState;
    private final float acceleration = 1000;
    private final float brakeForce = 100;
    private Spatial terrainGeo;
    private float steeringValue = 0;
    private float accelerationValue = 0;

    private DoesitRoverModern roverState;
    private ArmModule roverArm;
    private CameraModule roverCamera;
    private CarModule carModule;
            
    private Box rock = new Box(0.5f, 0.5f, 0.5f);
    private Camera mainCamera;
    private Geometry rockGeo[];
    private final int numberOfRocks=50;
    BitmapText hoverEnergyText;
    WorldTimeState worldTimeState;
    
    @Override
    public void simpleInitApp() {
       // flyCam.setMoveSpeed(60);
        assetManager.registerLoader(com.jme3.material.plugins.NeoTextureMaterialLoader.class,"tgr");
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        hoverEnergyText = new BitmapText(guiFont);
        hoverEnergyText.setSize(guiFont.getCharSet().getRenderedSize());
        hoverEnergyText.move(settings.getWidth()/2.0f, hoverEnergyText.getLineHeight(), 0);

        guiNode.attachChild(hoverEnergyText);
        

        worldTimeState = new WorldTimeState();
        stateManager.attach(worldTimeState);
        
        configureSky();
        configureCamera();
        configureObjects();
        configurePhysics();        
        
        roverState = new DoesitRoverModern(this);
        roverArm = new ArmModule(roverState);
        roverCamera = new CameraModule(roverState);
        carModule = new CarModule(roverState,400);
        carModule.setObject("WorldTime", worldTimeState);
        roverState.addModule(CarModule.class, carModule);
        roverState.addModule(ArmModule.class, roverArm);
        roverState.addModule(CameraModule.class, roverCamera);
        
        stateManager.attach(roverState);
        
        configureNavigationKey();
    }

    public BulletAppState getBulletAppState() {
        return bulletAppState;
    }
    
    @Override
    public void simpleUpdate(float tpf) {
        hoverEnergyText.setText("Hover Energy: " + carModule.getBattery().getPower());
        updateCam();
    }

    public Node getSceneNode() {
        return sceneNode;
    }
    
    private void updateCam() {
        cam.setLocation(roverState.getNode().getLocalTranslation().add(new Vector3f(-10,25, -10)));
        cam.lookAt(carModule.getControl().getPhysicsLocation(), Vector3f.UNIT_Y);
    }

    private void configureCamera() {
        setDisplayFps(true);
        setDisplayStatView(false);
        mainCamera = cam;
        cam.setLocation(new Vector3f(20,35,20));
        flyCam.setEnabled(false);
    }

    public Camera getMainCamera() {
        return mainCamera;
    }

    private void configureNavigationKey() {
        inputManager.addMapping("Forward", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Back", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Rotate Left", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Rotate Right", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Brake", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("Arm turn left", new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping("Arm turn right", new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addMapping("Grab", new KeyTrigger(KeyInput.KEY_G));
        inputManager.addMapping("Arm turn down", new KeyTrigger(KeyInput.KEY_DOWN));
        inputManager.addMapping("Arm turn up", new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping("Cam turn down", new KeyTrigger(KeyInput.KEY_J));
        inputManager.addMapping("Cam turn up", new KeyTrigger(KeyInput.KEY_U));
        inputManager.addMapping("Cam turn right", new KeyTrigger(KeyInput.KEY_K));
        inputManager.addMapping("Cam turn left", new KeyTrigger(KeyInput.KEY_H));
        inputManager.addMapping("Shorten arm", new KeyTrigger(KeyInput.KEY_E));
        inputManager.addMapping("Stretche arm", new KeyTrigger(KeyInput.KEY_R));
        inputManager.addMapping("Light on", new KeyTrigger(KeyInput.KEY_L));
        
        
        inputManager.addListener(this, "Rotate Left", "Rotate Right",
                "Arm turn left", "Arm turn down", "Stretche arm", "Shorten arm");
        
        inputManager.addListener(this, "Rotate Left", "Rotate Right",
                "Arm turn right", "Arm turn up", "Light on");
        
        inputManager.addListener(this, "Forward", "Back", "Brake","Grab",
                "Arm turn down", "Arm turn up", "Cam turn down", "Cam turn up",
                "Cam turn right", "Cam turn left");
    }

    private void configureObjects() {
        sceneNode = new Node("Scene");
            

        rootNode.attachChild(sceneNode);

        terrainGeo = assetManager.loadModel("/Models/Terrain/MarsTerrain.j3o");
        sceneNode.attachChild(terrainGeo);
        terrainGeo.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
                
        rockGeo = new Geometry[numberOfRocks];
        for (int i = 0; i < numberOfRocks; i++) {
            rockGeo[i] = new Geometry("Rock" + i + "Collectable", rock);
            Material rockMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            rockMat.setColor("Diffuse", ColorRGBA.Red);
            rockGeo[i].setMaterial(rockMat);
            rockGeo[i].setShadowMode(RenderQueue.ShadowMode.Cast);
            int x = FastMath.rand.nextInt(20)-10;
            int y = 50;
            int z = FastMath.rand.nextInt(20)-10;
            rockGeo[i].move(new Vector3f(x, y, z));
            sceneNode.attachChild(rockGeo[i]);
        }
    }
    
    private void configurePhysics() {
        //PHYSICS CONFIGURATION
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
       // bulletAppState.setDebugEnabled(true);
        bulletAppState.getPhysicsSpace().setGravity(new Vector3f(0.0f, -9.8f, 0.0f));
        bulletAppState.getPhysicsSpace().setAccuracy(0.007f);

        //ROCK CONFIGURATION
        for (int i = 0; i < numberOfRocks; i++) {
            RigidBodyControl rockControl = 
                new RigidBodyControl(
                new BoxCollisionShape(new Vector3f(0.6f,0.6f,0.6f)), 0.2f
            );
            rockGeo[i].addControl(rockControl);
            bulletAppState.getPhysicsSpace().add(rockControl) ;
          
        }
        
        //TERRAIN CONFIGURATION
        bulletAppState.getPhysicsSpace().add(terrainGeo.getControl(RigidBodyControl.class));
    }

    private void configureSky() {
        viewPort.setBackgroundColor(new ColorRGBA(0.9f, 0.8f, 0.8f, 1.0f));
    }
    
    public Vector3f getColorOf(Material mat, Vector3f pos, Vector3f normal, 
            Vector3f result) {
        return worldTimeState.getColorOf(mat, pos, normal, result);
    }
    
    public ColorRGBA getReflectionColor(Material mat, Vector3f pos, Vector3f normal, ColorRGBA result) {
        worldTimeState.getReflectionColor(mat, pos, normal, result, 0);
        return result;
    }
    
    public void onAction(String name, boolean isPressed, float tpf) {
       if (name.equals("Rotate Left")) {
            if (isPressed) {
                steeringValue += -0.5f;
            } else {
                steeringValue += 0.5f;
            }
            carModule.steer(steeringValue);
        } else if (name.equals("Rotate Right")) {
            if (isPressed) {
                steeringValue += 0.5f;
            } else {
                steeringValue -= 0.5f;
            }
            carModule.steer(steeringValue);
        } else if (name.equals("Forward")) {
            if (isPressed) {
                accelerationValue += acceleration;
            } else {
                accelerationValue -= acceleration;
            }
            carModule.accelerate(accelerationValue);
        } else if (name.equals("Back")) {
            if (isPressed) {
                accelerationValue -= acceleration;
            } else {
                accelerationValue += acceleration;
            }
            carModule.accelerate(accelerationValue);
        } else if (name.equals("Brake")) {
            
            if (isPressed) {
                carModule.brake(brakeForce);
            } else {
                carModule.brake(0);
            }
        } else if (name.equals("Arm turn left")) {
            if (isPressed){
                roverArm.act("TurnLeft");
            }
        } else if (name.equals("Arm turn right")) {
            if (isPressed){
                roverArm.act("TurnRight");
            }             
        } else if (name.equals("Arm turn down")) {
            if (isPressed) {
                roverArm.act("TurnDown");
            }
        } else if (name.equals("Arm turn up")) {
            if (isPressed){
                roverArm.act("TurnUp");
            }             
        } else if (name.equals("Grab")) {
            if (isPressed) {
                roverArm.act("Grab");
            } else {
                roverArm.act("Drop");
            }
        } else if (name.equals("Cam turn down")) {
            if (isPressed) {
                roverCamera.cameraTurnDown();
            }
        } else if (name.equals("Cam turn up")) {
            if (isPressed) {
                roverCamera.cameraTurnUp();
            }
        } else if (name.equals("Cam turn right")) {
            if (isPressed) {
                roverCamera.cameraTurnRight();
            }
        } else if (name.equals("Cam turn left")) {
            if (isPressed) {
                roverCamera.cameraTurnLeft();
            }
        } else if (name.equals("Stretche arm")) {
            if (isPressed) {
                roverArm.act("Sketche");
            }
        } else if (name.equals("Shorten arm")){
            if (isPressed) {
                roverArm.act("Shorten");
            }
        } else if (name.equals("Light on")) {
            if (isPressed) {
                roverCamera.setLightOn(!roverCamera.isLightOn());
            }
        }
    }
}
