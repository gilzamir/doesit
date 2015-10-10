package gilzamir.doesit.sim;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.font.BitmapText;
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

public class SimulationApp extends SimpleApplication  {

    private Node sceneNode;
    private BulletAppState bulletAppState;
    private Spatial terrainGeo;


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
    private Node collectableNode = new Node("Collectable");
    private AbstractController controller;
    
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
        controller = new KeybordController();
        controller.setup(this, roverState);
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
            collectableNode.attachChild(rockGeo[i]);
        }
        sceneNode.attachChild(collectableNode);
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
}
