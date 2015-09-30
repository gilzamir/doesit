package gilzamir.doesit.test;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.light.SpotLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.util.TangentBinormalGenerator;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;

/**
 * test
 * @author normenhansen
 */
public class HoverTank extends SimpleApplication {

    private static AppSettings settings;
    
    public static void main(String[] args) {
        settings = new AppSettings(true);
        settings.setTitle("Does it!");
        settings.setSettingsDialogImage("Interface/splashscreen.jpg");
        HoverTank app = new HoverTank();
        app.setSettings(settings);
        app.start();
    }

    private SpotLight spot;
    
    @Override
    public void simpleInitApp() {

        spot = new SpotLight();
        spot.setSpotRange(1000);
        spot.setSpotOuterAngle(20 * FastMath.DEG_TO_RAD);
        spot.setSpotInnerAngle(15 * FastMath.DEG_TO_RAD);
        rootNode.addLight(spot);
        
        
//        AmbientLight ambient = new AmbientLight();
//        ambient.setColor(ColorRGBA.White.mult(2f));
//        rootNode.addLight(ambient);
     
//        PointLight lamp = new PointLight();
//        lamp.setPosition(Vector3f.ZERO);
//        lamp.setColor(ColorRGBA.Yellow);
//        rootNode.addLight(lamp);
  
        
//        DirectionalLight sun = new DirectionalLight();
//        sun.setDirection(new Vector3f(-1, -1, 1));
//        sun.setColor(ColorRGBA.White);
//        rootNode.addLight(sun);
        
        Node tank = (Node) assetManager.loadModel(
                "Models/HoverTank/Tank.j3o");
        
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        TextureKey tankDiffuse = new TextureKey("Models/HoverTank/tank_diffuse.jpg", false);
        mat.setTexture("DiffuseMap", assetManager.loadTexture(tankDiffuse));
        
        TangentBinormalGenerator.generate(tank);
        
        TextureKey tankNormal = new TextureKey("Models/HoverTank/tank_normals.png", false);
        mat.setTexture("NormalMap", assetManager.loadTexture(tankNormal));
        
        TextureKey tankSpecular = new TextureKey("Models/HoverTank/tank_specular.jpg", false);
        mat.setTexture("SpecularMap", assetManager.loadTexture(tankSpecular));
        mat.setBoolean("UseMaterialColors", true);
        mat.setColor("Ambient", ColorRGBA.Gray);
        mat.setColor("Diffuse", ColorRGBA.White);
        mat.setColor("Specular", ColorRGBA.White);
        mat.setFloat("Shininess", 100f);
        
        
        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        viewPort.addProcessor(fpp);
        
        BloomFilter bloom = new BloomFilter(BloomFilter.GlowMode.SceneAndObjects);
        
        fpp.addFilter(bloom);
        
        TextureKey tankGlow = new TextureKey("Models/HoverTank/tank_glow_map.jpg", false);
        mat.setTexture("GlowMap", assetManager.loadTexture(tankGlow));
        mat.setColor("GlowColor", ColorRGBA.White);
        
        tank.setMaterial(mat);
        
        
        Box floorMesh = new Box(new Vector3f(-20, -2, -20), new Vector3f(20, -3, 20));
        floorMesh.scaleTextureCoordinates(new Vector2f(8,8));
        
        Geometry floorGeo = new Geometry("floor", floorMesh);
        Material floorMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        floorMat.setTexture("DiffuseMap", assetManager.loadTexture("Textures/BrickWall_diffuse.jpg"));
        floorMat.setTexture("NormalMap", assetManager.loadTexture("Textures/BrickWall_normal.jpg"));
        floorMat.getTextureParam("NormalMap").getTextureValue().setWrap(Texture.WrapMode.Repeat);
        floorMat.getTextureParam("DiffuseMap").getTextureValue().setWrap(Texture.WrapMode.Repeat);
        floorGeo.setMaterial(floorMat);
        rootNode.attachChild(floorGeo);
        
        
        cam.setLocation(new Vector3f(-4.0f, 10, -4.0f));
        cam.setAxes(cam.getLeft(), Vector3f.UNIT_Y, new Vector3f(0,-1,0));
        rootNode.attachChild(tank);
        
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
        spot.setDirection(cam.getDirection());
        spot.setPosition(cam.getLocation());
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
}
