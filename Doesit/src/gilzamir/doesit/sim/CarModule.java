package gilzamir.doesit.sim;

import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.util.ArrayList;
import java.util.List;

public class CarModule extends AbstractModule {
    private VehicleControl roverControl;
    private CollisionShape roverShape;
    private float mass;
    private float stiffness = 60.0f, compValue = 0.4f, dumpValue = 0.5f;
    private float maxSuspensionForce = 10000.0f;
    private Vector3f wheelDirection = new Vector3f(0,-1,0);
    private Vector3f wheelAxle = new Vector3f(-1,0,0);
    private Node wheelNode1, wheelNode2, wheelNode3, wheelNode4;
    private List<String> moduleNames = new ArrayList<String>();
    private float wheelRadius = 0.5f, yOff = 0.9f, xOff = 3.0f, zOff = 3.6f, restLenght = 0.4f;
    
    public CarModule(DoesitRoverModernState rover) {
        super(rover);
    }
    
    @Override
    public void setup(Node model) {
    }

    @Override
    public void act(String actionName) {
    }

    @Override
    public void update() {
        super.update();
    }
}
