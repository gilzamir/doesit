package gilzamir.doesit.test;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Sphere;
import com.jme3.system.AppSettings;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

/**
 * test
 * @author normenhansen
 */
public class MaterialUnshaded extends SimpleApplication {

    private static AppSettings settings;
    
    public static void main(String[] args) {
        settings = new AppSettings(true);
        settings.setTitle("Does it!");
        settings.setSettingsDialogImage("Interface/splashscreen.jpg");
        MaterialUnshaded app = new MaterialUnshaded();
        app.setSettings(settings);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        Sphere sphereMesh = new Sphere(16, 16, 1);
        Geometry sphereGeo = new Geometry("Unshaded textured sphere..", sphereMesh);
        sphereGeo.move(-2.0f, 0.0f, 0.0f);
        sphereGeo.rotate(FastMath.DEG_TO_RAD * -90, FastMath.DEG_TO_RAD * 120, 0.0f);
        
        Material sphereMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        sphereMat.setTexture("ColorMap", assetManager.loadTexture("Interface/Monkey.png"));
        sphereMat.setTexture("LightMap", assetManager.loadTexture("Interface/Monkey_light.png"));
        sphereGeo.setMaterial(sphereMat);
        rootNode.attachChild(sphereGeo);
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
