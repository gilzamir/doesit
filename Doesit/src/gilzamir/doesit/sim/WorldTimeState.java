package gilzamir.doesit.sim;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
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
        Vector3f ldir = l.getDirection().negate();
        
        float dayRate = Math.max(0, ldir.dot(Vector3f.UNIT_Y));
        
        
        ColorRGBA lcolor = l.getColor();
        float alfa = normal.dot(ldir);
        alfa = Math.max(0.0f, Math.min(alfa, 1.0f));
        result.x += lcolor.getRed() * alfa * diffuse.getRed() * dayRate;
        result.y += lcolor.getGreen() * alfa * diffuse.getGreen() * dayRate;
        result.z += lcolor.getBlue() * alfa * diffuse.getBlue() * dayRate;
        
        return result;
    }
    
    private Ray lightRay = new Ray();
    private int maxReflections = 3;
    public void getReflectionColor(Material mat, Vector3f from, Vector3f normal, ColorRGBA result, int counter) {
        lightRay.setOrigin(from);
        Vector3f dir = sunModel.getWorldTranslation().subtract(from);
        dir.normalize();
        lightRay.setDirection(dir);
        CollisionResults colresults = new CollisionResults();
       
        app.getSceneNode().collideWith(lightRay, colresults);
       
        
        if (colresults.size() > 0) {
            CollisionResult colresult = colresults.getClosestCollision();
           
            if (colresult.getGeometry() == sunModel) {
               Vector3f c = getColorOf(mat, dir, normal, dir);
               result.r = c.x;
               result.g = c.y;
               result.b = c.z;
            } else if (counter <= maxReflections) {
               
                Vector3f newFrom = colresult.getContactPoint();
                Vector3f newNormal = colresult.getContactNormal();
                ColorRGBA blend = new ColorRGBA(0,0,0,1);
                getReflectionColor(mat, newFrom, newNormal, blend, counter+1);
                result.r += blend.r;
                result.g += blend.g;
                result.b += blend.b;
                result.r = Math.min(1.0f, result.r);
                result.g = Math.min(1.0f, result.g);
                result.b = Math.min(1.0f, result.b);
            } else { 
                result.r = 0.0f;
                result.b = 0.0f;
                result.g = 0.0f;
            }
        } else {
                result.r = 0.0f;
                result.b = 0.0f;
                result.g = 0.0f;            
        }
    }
}
