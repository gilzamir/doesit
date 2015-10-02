package gilzamir.doesit.sim;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.Light;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.shadow.EdgeFilteringMode;

public class SimulationApp extends SimpleApplication implements ActionListener {

    private Node sceneNode;
    private BulletAppState bulletAppState;
    private final float acceleration = 1000;
    private final float brakeForce = 100;
    private Spatial terrainGeo;
    private float steeringValue = 0;
    private float accelerationValue = 0;

    private DoesitRoverModern rover;
    private Box rock = new Box(0.5f, 0.5f, 0.5f);
    private Camera mainCamera;
    private Geometry rockGeo[];
    private final int numberOfRocks=50;
    private DirectionalLight sunLight;
    private AmbientLight ambientLight;
    BitmapText hoverEnergyText;
    private Geometry sunModel;
    private Node sun;
    private float sunRotation = 0.0f;
    private long lastTime;
    
    @Override
    public void simpleInitApp() {
        flyCam.setMoveSpeed(60);
        assetManager.registerLoader(com.jme3.material.plugins.NeoTextureMaterialLoader.class,"tgr");
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        hoverEnergyText = new BitmapText(guiFont);
        hoverEnergyText.setSize(guiFont.getCharSet().getRenderedSize());
        hoverEnergyText.move(settings.getWidth()/2.0f, hoverEnergyText.getLineHeight(), 0);
        lastTime = System.currentTimeMillis();
        
        guiNode.attachChild(hoverEnergyText);
        
        configureSky();
       // configureCamera();
        configureObjects();
        configureLight();
        configurePhysics();
       // configureNavigationKey();
    }

    private Quaternion sunQuat = new Quaternion();
    private Vector3f sunPos = new Vector3f(0,0,0);
    @Override
    public void simpleUpdate(float tpf) {
        hoverEnergyText.setText("Hover Energy: " + rover.getEnergy());
        //updateCam();
      //  rover.update(tpf);
        final long updateTime = 200;
        long currentTime = System.currentTimeMillis();
        long delta = currentTime - lastTime;
        if (delta > updateTime){
            lastTime = currentTime;
            sunRotation += 0.009f;
            
            if (sunRotation >= 2 * FastMath.PI) {
                sunRotation = 0.0f;
            }
            
          sun.setLocalRotation(sunQuat.fromAngles(0, 0 , sunRotation));
          updateLights();
        }
    }
    
    private void updateCam() {
        cam.setLocation(rover.getNode().getLocalTranslation().add(new Vector3f(-10,25, -10)));
        cam.lookAt(rover.getControl().getPhysicsLocation(), Vector3f.UNIT_Y);
    }

    private void configureCamera() {
        setDisplayFps(false);
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
        
        inputManager.addListener(this, "Rotate Left", "Rotate Right",
                "Arm turn left", "Arm turn down", "Stretche arm", "Shorten arm");
        
        inputManager.addListener(this, "Rotate Left", "Rotate Right",
                "Arm turn right", "Arm turn up");
        
        inputManager.addListener(this, "Forward", "Back", "Brake","Grab",
                "Arm turn down", "Arm turn up", "Cam turn down", "Cam turn up",
                "Cam turn right", "Cam turn left");
    }

    private void configureObjects() {
        sceneNode = new Node("First Mission");

        Material sunMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            sunMat.setColor("Color", ColorRGBA.Yellow);
            
        Sphere sphere = new Sphere(16, 16, 1.0f);
        sunModel = new Geometry("Sun",sphere);
        sunModel.setMaterial(sunMat);
        sun = new Node("Sun");
        sun.attachChild(sunModel);
        sceneNode.attachChild(sun);
        
        sunModel.setLocalTranslation(0, 400, 0);
        
        rover = new DoesitRoverModern(500, 1000);
        
        rover.setupGeometry(this, sceneNode);

        
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

        //ROVER CONFIGURATION
        rover.setupPhysics(bulletAppState);

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
    
    private void configureLight() {
        

        ambientLight = new AmbientLight();
        rootNode.addLight(ambientLight);
        
        sunLight = new DirectionalLight();
        rootNode.addLight(sunLight);
        updateLights();
                final int SHADOW_MAP_SIZE = 1024;
        DirectionalLightShadowRenderer dlsr  = new DirectionalLightShadowRenderer(assetManager,
                SHADOW_MAP_SIZE, 4);
        dlsr.setLight(sunLight);
        viewPort.addProcessor(dlsr);
        
        DirectionalLightShadowFilter dlsf = new DirectionalLightShadowFilter(assetManager, SHADOW_MAP_SIZE, 4);
        dlsf.setLight(sunLight);
        dlsf.setEnabled(true);
        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        fpp.addFilter(dlsf);
        viewPort.addProcessor(fpp);
    }
    
    private void updateLights() {
        Vector3f from = sunModel.getWorldTranslation();
        Vector3f to = sun.getWorldTranslation();
        sunLight.setDirection(to.subtract(from).normalize());
    }

    public Vector3f getColorOf(Material mat, Vector3f pos, Vector3f normal, 
            Vector3f result) {
        ColorRGBA diffuse = (ColorRGBA)mat.getParam("Diffuse").getValue();
        ColorRGBA ambient = (ColorRGBA)mat.getParam("Ambient").getValue();
        normal.normalizeLocal();
        result.x = 0;
        result.y = 0;
        result.z = 0;
        
        ColorRGBA lamb = ambientLight.getColor();
        
        result.x = ambient.r * lamb.r;
        result.y = ambient.g * lamb.g;
        result.z = ambient.b * lamb.b;
        

        DirectionalLight l = sunLight;
        Vector3f ldir = l.getDirection();
        ColorRGBA lcolor = l.getColor();
        float alfa = normal.dot(ldir);
        alfa = Math.max(0.0f, Math.min(alfa, 1.0f));
        result.x += lcolor.getRed() * alfa * diffuse.getRed();
        result.y += lcolor.getGreen() * alfa * diffuse.getGreen();
        result.z += lcolor.getBlue() * alfa * diffuse.getBlue();
        
        return result;
    }
    
    public void onAction(String name, boolean isPressed, float tpf) {
        if (name.equals("Rotate Left")) {
            if (isPressed) {
                steeringValue += -0.5f;
            } else {
                steeringValue += 0.5f;
            }
            rover.steer(steeringValue);
        } else if (name.equals("Rotate Right")) {
            if (isPressed) {
                steeringValue += 0.5f;
            } else {
                steeringValue -= 0.5f;
            }
            rover.steer(steeringValue);
        } else if (name.equals("Forward")) {
            if (isPressed) {
                accelerationValue += acceleration;
            } else {
                accelerationValue -= acceleration;
            }
            rover.accelerate(accelerationValue);
        } else if (name.equals("Back")) {
            if (isPressed) {
                accelerationValue -= acceleration;
            } else {
                accelerationValue += acceleration;
            }
            rover.accelerate(accelerationValue);
        } else if (name.equals("Brake")) {
            
            if (isPressed) {
                rover.brake(brakeForce);
            } else {
                rover.brake(0);
            }
        } else if (name.equals("Arm turn left")) {
            if (isPressed){
                rover.armTurnLeft();
            }
        } else if (name.equals("Arm turn right")) {
            if (isPressed){
                rover.armTurnRight();
            }             
        } else if (name.equals("Arm turn down")) {
            if (isPressed) {
                rover.armTurnDown();
            }
        } else if (name.equals("Arm turn up")) {
            if (isPressed){
                rover.armTurnUp();
            }             
        } else if (name.equals("Grab")) {
            if (isPressed) {
                rover.grabberGrab();
            } else {
                rover.grabberDrop();
            }
        } else if (name.equals("Cam turn down")) {
            if (isPressed) {
                rover.cameraTurnDown();
            }
        } else if (name.equals("Cam turn up")) {
            if (isPressed) {
                rover.cameraTurnUp();
            }
        } else if (name.equals("Cam turn right")) {
            if (isPressed) {
                rover.cameraTurnRight();
            }
        } else if (name.equals("Cam turn left")) {
            if (isPressed) {
                rover.cameraTurnLeft();
            }
        } else if (name.equals("Stretche arm")) {
            if (isPressed) {
                rover.sketcheArm();
            }
        } else if (name.equals("Shorten arm")){
            if (isPressed) {
                rover.shortenArm();
            }
        }
    }
}
