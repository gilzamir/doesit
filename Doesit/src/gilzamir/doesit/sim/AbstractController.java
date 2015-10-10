package gilzamir.doesit.sim;

import com.jme3.app.SimpleApplication;

public abstract class AbstractController {
    public abstract void setup(SimpleApplication app, AbstractActor actor);
    public abstract void update();
}
