/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gilzamir.doesit.test;

import com.jme3.collision.CollisionResults;
import com.jme3.math.Ray;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.control.AbstractControl;

/**
 *
 * @author gilzamir
 */
public class CubeChaserControl extends AbstractControl {

    private Ray ray = new Ray();
    private final Camera cam;
    private final Node rootNode;
    
    public CubeChaserControl(Camera cam, Node rootNode) {
        this.cam = cam;
        this.rootNode = rootNode;
    }
    
    @Override
    protected void controlUpdate(float tpf) {
        spatial.rotate(tpf, tpf, tpf);
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }
    
    public void hello() {
        System.out.println("Hello! My type is "  + spatial.getName() );
    }
}
