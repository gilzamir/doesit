package gilzamir.doesit.sim.ai.test;

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.Light;
import com.jme3.light.SpotLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import multinet.net.NeuralNet;

public class SkinnerApp extends SimpleApplication implements ActionListener {

    private Node redNode, greenNode, blueNode;
    private Node actor;
    private Node model;
    private NeuralNet neuralNet;
    private SpotLight lightRed, lightGreen, lightBlue;
    private OutputStreamWriter log1, log2;
    private float sumIntensity = 0.0f;
    private SkinnerLikeEvaluator skinnerBoxImpl;
    private DirectionalLight light1, light2;
    
    @Override
    public void simpleInitApp() {
        model = (Node)assetManager.loadModel("Models/skinner/skinner.j3o");
        redNode = (Node)model.getChild("LightRed");
        greenNode = (Node)model.getChild("LightGreen");
        blueNode = (Node)model.getChild("LightBlue");
        actor = (Node)model.getChild("Actor");
        rootNode.attachChild(model);
        
        lightRed = new SpotLight();
        lightRed.setName("RED");
        lightRed.setColor(ColorRGBA.Red);
        lightRed.setPosition(redNode.getWorldTranslation());
        lightRed.setDirection((actor.getWorldTranslation().subtract(redNode.getWorldTranslation())).normalizeLocal());
        lightRed.setSpotInnerAngle(0f * FastMath.DEG_TO_RAD); // inner light cone (central beam)
        lightRed.setSpotOuterAngle(180f * FastMath.DEG_TO_RAD);
        
        lightGreen = new SpotLight();
        lightGreen.setName("GREEN");
        lightGreen.setColor(ColorRGBA.Green);
        lightGreen.setPosition(greenNode.getWorldTranslation());
        lightGreen.setDirection((actor.getWorldTranslation().subtract(greenNode.getWorldTranslation())).normalizeLocal());
        lightGreen.setSpotInnerAngle(0f * FastMath.DEG_TO_RAD); // inner light cone (central beam)
        lightGreen.setSpotOuterAngle(180f * FastMath.DEG_TO_RAD);
        
        lightBlue = new SpotLight();
        lightBlue.setName("BLUE");
        lightBlue.setColor(ColorRGBA.Blue);
        lightBlue.setPosition(blueNode.getWorldTranslation());
        lightBlue.setDirection((actor.getWorldTranslation().subtract(blueNode.getWorldTranslation())).normalizeLocal());     
        lightBlue.setSpotInnerAngle(0f * FastMath.DEG_TO_RAD); // inner light cone (central beam)
        lightBlue.setSpotOuterAngle(180f * FastMath.DEG_TO_RAD);
        
        
        light1 = new DirectionalLight();
        light1.setDirection( (new Vector3f(1.0f, 1.0f, 1.0f)).normalize());
        light1.setColor(new ColorRGBA(sumIntensity, sumIntensity, sumIntensity, 1.0f));
        
        light2 = new DirectionalLight();
        light2.setDirection( (new Vector3f(-1.0f, -1.0f, -1.0f)).normalize());
        light2.setColor(new ColorRGBA(sumIntensity, sumIntensity, sumIntensity, 1.0f));
        
        AmbientLight amb = new AmbientLight();
        amb.setColor(ColorRGBA.LightGray);
        
        rootNode.addLight(amb);
        rootNode.addLight(lightRed);
        rootNode.addLight(lightGreen);
        rootNode.addLight(lightBlue);
        rootNode.addLight(light1);
        rootNode.addLight(light2);
        
        flyCam.setEnabled(false);
        cam.setLocation(new Vector3f(-15.320519f, 12.324081f, 14.880732f));
        cam.lookAtDirection(new Vector3f(0.67468536f, -0.37986183f, -0.6328541f), Vector3f.UNIT_Y);    
    

        inputManager.addMapping("ToogleNoise", new KeyTrigger(KeyInput.KEY_N));
        inputManager.addMapping("ShowOutput", new KeyTrigger(KeyInput.KEY_O));
        inputManager.addMapping("ShowEnergy", new KeyTrigger(KeyInput.KEY_E));
        inputManager.addMapping("ShowDopamine", new KeyTrigger(KeyInput.KEY_D));
        
        inputManager.addListener(this, "ToogleNoise", "ShowOutput", "ShowEnergy", "ShowDopamine");
        
        try {
            log1 = new OutputStreamWriter(new FileOutputStream("log1.txt"));
            log2 = new OutputStreamWriter(new FileOutputStream("log2.txt"));
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        
    }

    public void setNeuralNet(NeuralNet neuralNet) {
        this.neuralNet = neuralNet;
    }
    
    public void setSkinnerBoxImpl(SkinnerLikeEvaluator ev) {
        this.skinnerBoxImpl = ev;
    }

    
    private long delay = 300;
    public void setDelay(long delay) {
        this.delay = delay;
    }

    public long getDelay() {
        return delay;
    }

    
    private long lastTime = -1;
    private ColorRGBA sumColor = new ColorRGBA();
    
    @Override
    public void update() {
        super.update(); //To change body of generated methods, choose Tools | Templates.
        
        if (skinnerBoxImpl != null) {
            if (lastTime  < 0) {
                if (skinnerBoxImpl != null) {
                    skinnerBoxImpl.reset();
                }
                lastTime = System.currentTimeMillis();
                neuralNet.randomizeMatrix(neuralNet.getWeight(), -50, 50);
            }
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastTime > delay) {
                lastTime = currentTime;
                skinnerBoxImpl.step(this.neuralNet);
                System.out.println("DemageInput: " + skinnerBoxImpl.demageInput);
                System.out.println("ShowOutput: " + skinnerBoxImpl.showOutput);
                System.out.println("ShowEnergy: " + skinnerBoxImpl.showEnergy);
            }
            setRedLight(this.skinnerBoxImpl.lightState[0]);
            setBlueLight(this.skinnerBoxImpl.lightState[1]);
            setBlueLight(this.skinnerBoxImpl.lightState[2]);
            
            sumIntensity = skinnerBoxImpl.getSunIntensity();
            
            sumColor.set(sumIntensity, sumIntensity, sumIntensity, 1.0f);
            
            light1.setColor(sumColor);
            light2.setColor(sumColor);
        }
    }

    public float getLightState(Light light) {
        if (light.getColor().equals(ColorRGBA.Black)) {
            return -0.5f;
        } else {
            return 0.5f;
        }
    }
    
    public void lightOn(Light light, ColorRGBA c, Node node, String meshName) {
        light.setColor(c);
        Geometry geo = (Geometry) node.getChild(meshName);
        geo.getMaterial().setColor("Diffuse", c);
    }
    
    public void lightOff(Light light, Node node, String meshName) {
        light.setColor(ColorRGBA.Black);
        Geometry geo = (Geometry) node.getChild(meshName);
        geo.getMaterial().setColor("Diffuse", ColorRGBA.Black);
    }
    
    
    public void setRedLight(float v) {
       ColorRGBA c = new ColorRGBA(v, 0.0f,0.0f, 1.0f);
       lightRed.setColor(c);
       Geometry geo = (Geometry) redNode.getChild("redMesh1");
       geo.getMaterial().setColor("Diffuse", c);
    }
    
    public void setBlueLight(float v) {
       ColorRGBA c = new ColorRGBA(0.0f, 0.0f, v, 1.0f);
       lightBlue.setColor(c);
       Geometry geo = (Geometry) blueNode.getChild("blueMesh1");
       geo.getMaterial().setColor("Diffuse", c);    
    }
    
    public void setGreenLight(float v) {
       ColorRGBA c = new ColorRGBA(0.0f, v, 0.0f, 0.0f);
       lightGreen.setColor(c);
       Geometry geo = (Geometry) greenNode.getChild("greenMesh1");
       geo.getMaterial().setColor("Diffuse", c);
    }

    public float getBlueLightInt() {
        return lightBlue.getColor().b;
    }
    
    public float getRedLightInt() {
        return lightRed.getColor().r;
    }
    
    public float getGreenLightInt() {
        return lightGreen.getColor().g;
    }
    
    public static void startApp(SkinnerLikeEvaluator ev, NeuralNet net) {
       SkinnerApp app = new SkinnerApp();
       app.setSkinnerBoxImpl(ev);
       AppSettings settings = new AppSettings(true);
       settings.setTitle("SkinnerBox");
       app.setSettings(settings);
       app.setShowSettings(false);
       app.setDisplayStatView(false);
       app.setDisplayFps(false);
       app.neuralNet = net;
       app.start();

    }

    public void onAction(String name, boolean isPressed, float tpf) {
        if (name.equals("ToogleNoise")) {
            if (isPressed) {
                if (skinnerBoxImpl != null) {
                    skinnerBoxImpl.demageInput = !skinnerBoxImpl.demageInput;
                }
            }
        } else if (name.equals("ShowOutput")) {
            if (isPressed) {
                if (skinnerBoxImpl != null) {
                    skinnerBoxImpl.showOutput = !skinnerBoxImpl.showOutput;
                }
            }
        } else if (name.equals("ShowEnergy")) {
            if (isPressed) {
                if (skinnerBoxImpl != null) {
                    skinnerBoxImpl.showEnergy = !skinnerBoxImpl.showEnergy;
                }
            }
        } else if (name.equals("ShowDopamine")) {
            if (isPressed) {
                if (skinnerBoxImpl != null) {
                    skinnerBoxImpl.showDopamine = !skinnerBoxImpl.showDopamine;
                }
            }
        }
    }

    @Override
    public void destroy() {
        super.destroy(); //To change body of generated methods, choose Tools | Templates.
        try {
            log1.close();
            log2.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    
    
    public static void main(String args[]) {
        startApp(null, null);
    }
}
