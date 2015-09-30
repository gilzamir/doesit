package gilzamir.doesit.test;

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

/**
 * test
 * @author normenhansen
 */
public class UserInput extends SimpleApplication {

    private static  final AppSettings settings = new AppSettings(true);
    
    
    private final static Trigger TRIGGER_COLOR = new KeyTrigger(KeyInput.KEY_SPACE);
    private final static Trigger TRIGGER_ROTATE = new MouseButtonTrigger(MouseInput.BUTTON_LEFT);
    
    private final static String MAPPING_COLOR = "Toggle Color";
    private final static String MAPPING_ROTATE = "Rotate";
    
    private Geometry boxGeom;
    
    public static void main(String[] args) {
        settings.setTitle("Does it!");
        settings.setSettingsDialogImage("Interface/splashscreen.jpg");
        //settings.setUseInput(false);
        UserInput app = new UserInput();
        app.setSettings(settings);
        app.start();
    }
    
    private ActionListener actionListener = new ActionListener() {
        public void onAction(String name, boolean isPressed, float tpf) {
            System.out.println("You triggered: " + name);
            boxGeom.getMaterial().setColor("Color", ColorRGBA.randomColor());
        }
    };
    
    private AnalogListener analogListener = new AnalogListener() {
        public void onAnalog(String name, float value, float tpf) {
            System.out.println("You triggered: " + name);
            boxGeom.rotate(0, value, 0);
        }
    };


    
    @Override
    public void simpleInitApp() {
        
        inputManager.addMapping(MAPPING_COLOR, TRIGGER_COLOR);
        inputManager.addMapping(MAPPING_ROTATE, TRIGGER_ROTATE);
        
        inputManager.addListener(actionListener, new String[]{MAPPING_COLOR});
        inputManager.addListener(analogListener, new String[]{MAPPING_ROTATE});
        Box boxShape;        
        boxShape = new Box(1, 1, 1);
        boxGeom = new Geometry("Box", boxShape);

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        boxGeom.setMaterial(mat);

        rootNode.attachChild(boxGeom);
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
