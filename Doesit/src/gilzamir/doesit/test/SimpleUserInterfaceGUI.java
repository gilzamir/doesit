package gilzamir.doesit.test;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
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
public class SimpleUserInterfaceGUI extends SimpleApplication {

    private static AppSettings settings;
    
    public static void main(String[] args) {
        settings = new AppSettings(true);
        settings.setTitle("Does it!");
        settings.setSettingsDialogImage("Interface/splashscreen.jpg");
        SimpleUserInterfaceGUI app = new SimpleUserInterfaceGUI();
        app.setSettings(settings);
        app.start();
    }

    private float distance = 0;
    BitmapText distanceText;
    BitmapText fancyText;
    
    @Override
    public void simpleInitApp() {
        setDisplayStatView(false);
        setDisplayFps(false);
        
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        distanceText = new BitmapText(guiFont);
        distanceText.setSize(guiFont.getCharSet().getRenderedSize());
        distanceText.move(settings.getWidth()/2.0f, distanceText.getLineHeight(), 0);
     
        
        BitmapFont customFont = assetManager.loadFont("Interface/Fonts/Constantia.fnt");
        fancyText = new BitmapText(customFont);
        
        guiNode.attachChild(distanceText);
        
        
        Box b = new Box(1, 1, 1);
        Geometry geom = new Geometry("Box", b);

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        geom.setMaterial(mat);

        rootNode.attachChild(geom);
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
        distance = Vector3f.ZERO.distance(cam.getLocation());
        distanceText.setText("Distance: " + distance);
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
}
