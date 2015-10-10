package gilzamir.doesit.sim;

import com.jme3.app.SimpleApplication;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;

public class KeybordController extends AbstractController implements ActionListener {

    private CarModule carModule;
    private CameraModule camModule;
    private ArmModule armModule;
    private float steeringValue = 0;
    private float accelerationValue = 0;
    private final float acceleration = 1000;
    private final float brakeForce = 100;
    
    @Override
    public void setup(SimpleApplication app, AbstractActor actor) {
        camModule = actor.getModule(CameraModule.class);
        armModule = actor.getModule(ArmModule.class);
        carModule = actor.getModule(CarModule.class);
        
        InputManager inputManager = app.getInputManager();
        
        inputManager.addMapping("Forward", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Back", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Rotate Left", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Rotate Right", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Brake", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("Arm turn left", new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping("Arm turn right", new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addMapping("Grab", new KeyTrigger(KeyInput.KEY_G));
        inputManager.addMapping("Arm turn down", new KeyTrigger(KeyInput.KEY_DOWN));
        inputManager.addMapping("Arm turn up", new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping("Cam turn down", new KeyTrigger(KeyInput.KEY_J));
        inputManager.addMapping("Cam turn up", new KeyTrigger(KeyInput.KEY_U));
        inputManager.addMapping("Cam turn right", new KeyTrigger(KeyInput.KEY_K));
        inputManager.addMapping("Cam turn left", new KeyTrigger(KeyInput.KEY_H));
        inputManager.addMapping("Shorten arm", new KeyTrigger(KeyInput.KEY_E));
        inputManager.addMapping("Stretche arm", new KeyTrigger(KeyInput.KEY_R));
        inputManager.addMapping("Light on", new KeyTrigger(KeyInput.KEY_L));
        
        
        inputManager.addListener(this, "Rotate Left", "Rotate Right",
                "Arm turn left", "Arm turn down", "Stretche arm", "Shorten arm");
        
        inputManager.addListener(this, "Rotate Left", "Rotate Right",
                "Arm turn right", "Arm turn up", "Light on");
        
        inputManager.addListener(this, "Forward", "Back", "Brake","Grab",
                "Arm turn down", "Arm turn up", "Cam turn down", "Cam turn up",
                "Cam turn right", "Cam turn left");
    }

    @Override
    public void update() {
        
    }

    public void onAction(String name, boolean isPressed, float tpf) {
   if (name.equals("Rotate Left")) {
            if (isPressed) {
                steeringValue += -0.5f;
            } else {
                steeringValue += 0.5f;
            }
            carModule.steer(steeringValue);
        } else if (name.equals("Rotate Right")) {
            if (isPressed) {
                steeringValue += 0.5f;
            } else {
                steeringValue -= 0.5f;
            }
            carModule.steer(steeringValue);
        } else if (name.equals("Forward")) {
            if (isPressed) {
                accelerationValue += acceleration;
            } else {
                accelerationValue -= acceleration;
            }
            carModule.accelerate(accelerationValue);
        } else if (name.equals("Back")) {
            if (isPressed) {
                accelerationValue -= acceleration;
            } else {
                accelerationValue += acceleration;
            }
            carModule.accelerate(accelerationValue);
        } else if (name.equals("Brake")) {
            
            if (isPressed) {
                carModule.brake(brakeForce);
            } else {
                carModule.brake(0);
            }
        } else if (name.equals("Arm turn left")) {
            if (isPressed){
                armModule.act("TurnLeft");
            }
        } else if (name.equals("Arm turn right")) {
            if (isPressed){
                armModule.act("TurnRight");
            }             
        } else if (name.equals("Arm turn down")) {
            if (isPressed) {
                armModule.act("TurnDown");
            }
        } else if (name.equals("Arm turn up")) {
            if (isPressed){
                armModule.act("TurnUp");
            }             
        } else if (name.equals("Grab")) {
            if (isPressed) {
                armModule.act("Grab");
            } else {
                armModule.act("Drop");
            }
        } else if (name.equals("Cam turn down")) {
            if (isPressed) {
                camModule.cameraTurnDown();
            }
        } else if (name.equals("Cam turn up")) {
            if (isPressed) {
                camModule.cameraTurnUp();
            }
        } else if (name.equals("Cam turn right")) {
            if (isPressed) {
                this.camModule.cameraTurnRight();
            }
        } else if (name.equals("Cam turn left")) {
            if (isPressed) {
                camModule.cameraTurnLeft();
            }
        } else if (name.equals("Stretche arm")) {
            if (isPressed) {
                armModule.act("Sketche");
            }
        } else if (name.equals("Shorten arm")){
            if (isPressed) {
                armModule.act("Shorten");
            }
        } else if (name.equals("Light on")) {
            if (isPressed) {
                camModule.setLightOn(!camModule.isLightOn());
            }
        }
    }
}
