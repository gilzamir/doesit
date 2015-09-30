package gilzamir.doesit.test;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.light.DirectionalLight;
import com.jme3.light.SpotLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

/**
 * test
 * @author normenhansen
 */
public class LoadModel extends SimpleApplication {

    private static AppSettings settings;
    
    public static void main(String[] args) {
        settings = new AppSettings(true);
        settings.setTitle("Does it!");
        settings.setSettingsDialogImage("Interface/splashscreen.jpg");
      
        LoadModel app = new LoadModel();
        app.setSettings(settings);
        app.start();
    }

    private static final String ANIM_IDLE = "idle";
    private static final String ANIM_WALK = "walk";
    
    private static final String MAPPING_WALK = "Walk Command";
    private static final KeyTrigger PRESS_W_TRIGGER = new KeyTrigger(KeyInput.KEY_P);
    
    
    private AnimControl control;
    private AnimChannel channel;
    
    private Spatial player;
    
    private ActionListener actionListener = new ActionListener() {
        public void onAction(String name, boolean isPressed, float tpf) {
            if (name.equals(MAPPING_WALK) && isPressed) {
                if (!channel.getAnimationName().equals(ANIM_WALK)) {
                    channel.setAnim(ANIM_WALK);
                }
                
                if (name.equals(MAPPING_WALK) && !isPressed) {
                    channel.setAnim(ANIM_IDLE);
                }
            }
        }
    };
    
    private AnalogListener analogListener = new AnalogListener() {
        public void onAnalog(String name, float value, float tpf) {
            if (name.equals(MAPPING_WALK)) {
                player.move(new Vector3f(0,0,tpf));
            }
        }
    };
    
    
    public void configurePlayer() {
        Node node = (Node)assetManager.loadModel("Textures/Alan/alan_anim.j3o");
        node = (Node)node.getChild("Armature");
        node = (Node)node.getChild("alan");
        player = node;
        player.scale(0.3f);
    }
    
    @Override
    public void simpleInitApp() {
    
        inputManager.addMapping(MAPPING_WALK, PRESS_W_TRIGGER);
        
        inputManager.addListener(actionListener, new String[]{MAPPING_WALK});
        inputManager.addListener(analogListener, new String[]{MAPPING_WALK});
        
        configurePlayer();
        
        control = player.getControl(AnimControl.class);
        
        channel = control.createChannel();
        channel.setAnim(ANIM_IDLE);
        channel.setSpeed(0.01f);
        for (String an : control.getAnimationNames()) {
            System.out.println(an);
        }
        
        rootNode.attachChild(player);
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-1.0f, -1.0f, -1.5f));
        sun.setColor(ColorRGBA.White);
        
        Box b = new Box(1, 1, 1);
        
        Geometry geom = new Geometry("Box", b);
       
        geom.setLocalScale(100, 1f, 100);
        geom.setLocalTranslation(0, -6, -10);
        
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.White);
        geom.setMaterial(mat);

        rootNode.attachChild(geom);
        rootNode.addLight(sun);
    }

    public void enableFullScreenMode() {
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        DisplayMode mode[] = device.getDisplayModes();
        settings.setResolution(mode[0].getWidth(), mode[0].getHeight());
        settings.setFrequency(mode[0].getRefreshRate());
        settings.setDepthBits(mode[0].getBitDepth());
        settings.setFullscreen(device.isFullScreenSupported());
    }
    
    @Override
    public void simpleUpdate(float tpf) {
        //TODO: add update code
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
}
