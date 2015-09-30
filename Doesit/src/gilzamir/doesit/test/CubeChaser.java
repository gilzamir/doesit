package gilzamir.doesit.test;

import com.jme3.app.SimpleApplication;
import com.jme3.collision.CollisionResults;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Ray;
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
public class CubeChaser extends SimpleApplication {

    private static AppSettings settings;
    
    public static void main(String[] args) {
        settings = new AppSettings(true);
        settings.setTitle("Does it!");
        settings.setSettingsDialogImage("Interface/splashscreen.jpg");
      //  settings.setUseInput(false);
        CubeChaser app = new CubeChaser();
        app.setSettings(settings);
        app.start();

    }

    


    @Override
    public void simpleInitApp() {
        flyCam.setMoveSpeed(100);
        CubeChaserState state = new CubeChaserState();
        stateManager.attach(state);
    }

    public static void enableFullScreenMode(AppSettings settings) {
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        DisplayMode mode[] = device.getDisplayModes();
        settings.setResolution(mode[0].getWidth(), mode[0].getHeight());
        settings.setFrequency(mode[0].getRefreshRate());
        settings.setDepthBits( (mode[0].getBitDepth() != 0 ? mode[0].getBitDepth() : 24) );
        settings.setFullscreen(device.isFullScreenSupported());
    }
    
    @Override
    public void simpleUpdate(float tpf) {
        if (stateManager.getState(CubeChaserState.class) != null) {
            System.out.println("Chase counter: " + 
                    stateManager.getState(CubeChaserState.class).getCounter());
        }
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }


}
