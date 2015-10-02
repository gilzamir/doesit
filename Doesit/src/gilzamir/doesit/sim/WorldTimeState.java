package gilzamir.doesit.sim;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.shadow.DirectionalLightShadowRenderer;

public class WorldTimeState extends AbstractAppState {
    private Geometry sunModel;
    private Node sun;
    private DirectionalLight sunLight;
    private AmbientLight ambientLight;
    private SimulationApp app;

    @Override
    public void update(float tpf) {
        updateLights();
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        this.app = (SimulationApp) app;
        Node sceneNode = this.app.getSceneNode();
        
        //BEGIN::CONFIGURE SUN GEOMETRY AND MATERIAL
        Sphere sphere = new Sphere(16, 16, 1.0f);
        Material sunMat = new Material(app.getAssetManager(),
                "Common/MatDefs/Misc/Unshaded.j3md");
        sunMat.setColor("Color", ColorRGBA.Yellow);
        sunModel = new Geometry("Sun", sphere);
        sunModel.setMaterial(sunMat);
        sun = new Node("Sun");
        sun.addControl(new SunControl());
        sun.attachChild(sunModel);
        sceneNode.attachChild(sun);
        sunModel.setLocalTranslation(0, 400, 0);
        configureLight();
    }
    
    private void updateLights() {
        Vector3f from = sunModel.getWorldTranslation();
        Vector3f to = sun.getWorldTranslation();
        sunLight.setDirection(to.subtract(from).normalize());
    }

    private void configureLight() {
        Node rootNode = app.getSceneNode();
        ambientLight = new AmbientLight();
        rootNode.addLight(ambientLight);
        
        sunLight = new DirectionalLight();
        rootNode.addLight(sunLight);
        updateLights();
        
        final int SHADOW_MAP_SIZE = 1024;
        DirectionalLightShadowRenderer dlsr  = 
                new DirectionalLightShadowRenderer(app.getAssetManager(), 
                SHADOW_MAP_SIZE, 4);
        dlsr.setLight(sunLight);
        app.getViewPort().addProcessor(dlsr);
        
        DirectionalLightShadowFilter dlsf = 
                new DirectionalLightShadowFilter(app.getAssetManager(),
                SHADOW_MAP_SIZE, 4);
        
        dlsf.setLight(sunLight);
        dlsf.setEnabled(true);
        FilterPostProcessor fpp = new FilterPostProcessor(app.getAssetManager());
        fpp.addFilter(dlsf);
        app.getViewPort().addProcessor(fpp);
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
}
