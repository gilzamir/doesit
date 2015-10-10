package gilzamir.doesit.sim.ai;

import com.jme3.app.SimpleApplication;
import gilzamir.doesit.sim.AbstractActor;
import gilzamir.doesit.sim.AbstractController;
import gilzamir.doesit.sim.ArmModule;
import gilzamir.doesit.sim.CameraModule;
import gilzamir.doesit.sim.CarModule;

public class NeuralNetController extends AbstractController  {

    private CarModule carModule;
    private CameraModule camModule;
    private ArmModule armModule;
    
    @Override
    public void setup(SimpleApplication app, AbstractActor actor) {
        carModule = actor.getModule(CarModule.class);
        camModule = actor.getModule(CameraModule.class);
        armModule = actor.getModule(ArmModule.class);
    }

    @Override
    public void update() {
    }
}
