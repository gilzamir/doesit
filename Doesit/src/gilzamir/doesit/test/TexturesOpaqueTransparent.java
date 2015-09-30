package gilzamir.doesit.test;

import com.jme3.app.SimpleApplication;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.system.AppSettings;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

/**
 * test
 * @author normenhansen
 */
public class TexturesOpaqueTransparent extends SimpleApplication {

    private static AppSettings settings;
    
    public static void main(String[] args) {
        settings = new AppSettings(true);
        settings.setTitle("Does it!");
        settings.setSettingsDialogImage("Interface/splashscreen.jpg");
        TexturesOpaqueTransparent app = new TexturesOpaqueTransparent();
        app.setSettings(settings);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        Sphere sphereMesh = new Sphere(16, 16, 1);
        Geometry sphereGeom = new Geometry("lit textured sphere", sphereMesh);
        
        Material sphereMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        sphereMat.setTexture("DiffuseMap", assetManager.loadTexture("Interface/Monkey.png"));
        sphereGeom.setMaterial(sphereMat);
        sphereGeom.move(-2f, 0f, 0f);
        sphereGeom.rotate(FastMath.DEG_TO_RAD * -90, FastMath.DEG_TO_RAD * 120, 0f);
        
        sphereMat.getAdditionalRenderState().setAlphaTest(true);
        sphereMat.getAdditionalRenderState().setAlphaFallOff(0.5f);
        
        sphereGeom.setQueueBucket(RenderQueue.Bucket.Transparent);
        
        rootNode.attachChild(sphereGeom);
        
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(1, 0, -2));
        sun.setColor(ColorRGBA.White);
        rootNode.addLight(sun);
        
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.White);
        rootNode.addLight(ambient);
        
        flyCam.setMoveSpeed(50);
        viewPort.setBackgroundColor(ColorRGBA.LightGray);
        
        
        Box windowMesh = new Box(1f, 1.4f, 0.01f);
        Geometry windowGeo = new Geometry("stained glass window", windowMesh);
        Material windowMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        windowMat.setTexture("DiffuseMap", assetManager.loadTexture("Textures/mucha-window.png"));
        windowMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        windowGeo.setMaterial(windowMat);
        windowGeo.setQueueBucket(RenderQueue.Bucket.Transparent);
        windowGeo.setMaterial(windowMat);
        windowGeo.move(1f, 0, 4f);
        rootNode.attachChild(windowGeo);
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
