/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gilzamir.doesit.test;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.collision.CollisionResults;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

/**
 *
 * @author gilzamir
 */
public class CubeChaserState extends AbstractAppState {

    private static Box mesh = new Box(1, 1, 1);
    
    private SimpleApplication app;
    private  Camera cam;
    private  Node rootNode;
    private AssetManager assetManager;
    private Ray ray = new Ray();
    private int counter = 0;
    
    @Override
    public void update(float tpf) {
        super.update(tpf); //To change body of generated methods, choose Tools | Templates.
        ray.setOrigin(cam.getLocation());
        ray.setDirection(cam.getDirection());
        CollisionResults results = new CollisionResults();
    
        rootNode.collideWith(ray, results);
        if (results.size() > 0) {
            Geometry target = results.getClosestCollision().getGeometry();
            
            if (target.getControl(CubeChaserControl.class) != null) {
                target.getControl(CubeChaserControl.class).hello();
                counter++;
                if (cam.getLocation().distance(target.getLocalTranslation()) < 10) {
                    target.move(cam.getDirection());
                   
                }
            }
        }
    }

    public int getCounter() {
        return counter;
    }

    @Override
    public void cleanup() {
        super.cleanup(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app); //To change body of generated methods, choose Tools | Templates.
        this.app = (SimpleApplication)app;
        this.cam = this.app.getCamera();
        this.rootNode = this.app.getRootNode();
        this.assetManager = this.app.getAssetManager();
        
        makeCubes(40);
    
    }
    
        
    private void makeCubes(int number) {
        for (int i = 0; i  < number; i++) {
            Vector3f loc = new Vector3f(
                    FastMath.nextRandomInt(-20, 20),
                    FastMath.nextRandomInt(-20, 20),
                    FastMath.nextRandomInt(-20, 20)
            );
            
            Geometry geom = myBox("Cube" + i, loc, ColorRGBA.randomColor());
            
            if (FastMath.nextRandomInt(1, 4) == 4) {
                geom.addControl(new CubeChaserControl(cam, rootNode));
            }
            
            rootNode.attachChild(geom);
        }
    }
    
    
    
    public Geometry myBox(String name, Vector3f loc, ColorRGBA color) {
        Geometry geom = new Geometry(name, mesh);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", color);
        geom.setMaterial(mat);
        geom.setLocalTranslation(loc);
        return geom;
    }
}
