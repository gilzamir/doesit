package gilzamir.doesit.sim;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;

import com.jme3.scene.control.AbstractControl;

public class SunControl extends AbstractControl {

    private float sunRotation = 0.0f;
    private long lastTime;
    private Quaternion sunQuat = new Quaternion();
    
    public SunControl() {
        lastTime = System.currentTimeMillis();
    }
    
    @Override
    protected void controlUpdate(float tpf) {
        final long updateTime = 200;
        long currentTime = System.currentTimeMillis();
        
        long delta = currentTime - lastTime;
        if (delta > updateTime) {
            lastTime = currentTime;
            sunRotation += 0.009f;

            if (sunRotation >= 2 * FastMath.PI) {
                sunRotation = 0.0f;
            }

            spatial.setLocalRotation(sunQuat.fromAngles(0, 0, sunRotation));
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }
}
