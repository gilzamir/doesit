package gilzamir.doesit.sim.ai.test;

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.Light;
import com.jme3.light.PointLight;
import com.jme3.light.SpotLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import java.util.Arrays;
import multinet.net.NeuralNet;

public class SkinnerApp extends SimpleApplication implements ActionListener {

    private Node redNode, greenNode, blueNode;
    private Node actor;
    private Node model;
    private NeuralNet neuralNet;
    private SpotLight lightRed, lightGreen, lightBlue;
    private boolean stimulate = false;
    private Light currentLight = null;
    
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
        
        
        PointLight light = new PointLight();
        light.setPosition(actor.getWorldTranslation().add(0, 10, 0));
        light.setColor(ColorRGBA.White);
        
        AmbientLight amb = new AmbientLight();
        amb.setColor(ColorRGBA.LightGray);
        
        rootNode.addLight(amb);
        rootNode.addLight(lightRed);
        rootNode.addLight(lightGreen);
        rootNode.addLight(lightBlue);
        rootNode.addLight(light);
        
        flyCam.setEnabled(false);
        cam.setLocation(new Vector3f(-15.320519f, 12.324081f, 14.880732f));
        cam.lookAtDirection(new Vector3f(0.67468536f, -0.37986183f, -0.6328541f), Vector3f.UNIT_Y);
    
    
        inputManager.addMapping("ToogleRed", new KeyTrigger(KeyInput.KEY_R));
        inputManager.addMapping("ToogleGreen", new KeyTrigger(KeyInput.KEY_G));        
        inputManager.addMapping("ToogleBlue", new KeyTrigger(KeyInput.KEY_B));        
        inputManager.addMapping("Stimulate", new KeyTrigger(KeyInput.KEY_S));
        
        
        inputManager.addListener(this, "ToogleRed", "ToogleGreen", "ToogleBlue",
                "Stimulate");
        
    }

    public void setNeuralNet(NeuralNet neuralNet) {
        this.neuralNet = neuralNet;
    }

    @Override
    public void update() {
        super.update(); //To change body of generated methods, choose Tools | Templates.
        if (stimulate && neuralNet != null) {
            int[] in = neuralNet.getInputs();
            neuralNet.setInput(in[0], getLightState(lightRed));
            neuralNet.setInput(in[1], getLightState(lightGreen));
            neuralNet.setInput(in[2], getLightState(lightBlue));
            neuralNet.setInput(in[3], 100);
            neuralNet.process();
            double out[] = neuralNet.getOutput();
            
            if (out[0] > 0.2) {
                lightOn(lightRed, ColorRGBA.Red, redNode, "redMesh1");
            } else if (out[0] < -0.2) {
                lightOff(lightRed, redNode, "redMesh1");
            }
            
            if (out[1] > 0.2) {
                lightOn(lightGreen, ColorRGBA.Green, greenNode, "greenMesh1");
            } else if (out[1] < -0.2) {
                lightOff(lightGreen, greenNode, "greenMesh1");
            }
            
            if (out[2] > 0.2) {
                lightOn(lightBlue, ColorRGBA.Blue, blueNode, "blueMesh1");
            } else if (out[2] < -0.2) {
                lightOff(lightBlue, blueNode, "blueMesh1");
            }
            
            System.out.println(Arrays.toString(out));
            stimulate = false;
        }
    }

    public float getLightState(Light light) {
        if (light.getColor().equals(ColorRGBA.Black)) {
            return 0.0f;
        } else {
            return 1.0f;
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
    
    public static void startApp(NeuralNet net) {
       SkinnerApp app = new SkinnerApp();
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
        if (name.equals("ToogleRed")) {
            if (isPressed) {
                if (getLightState(lightRed) == 1.0) {
                    lightOff(lightRed, redNode, "redMesh1");
                } else {
                    lightOn(lightRed, ColorRGBA.Red, redNode, "redMesh1");
                }
            }
        } else  if (name.equals("ToogleGreen")) {
            if (isPressed) {
                if (getLightState(lightGreen) == 1.0) {
                    lightOff(lightGreen, greenNode, "greenMesh1");
                } else {
                    lightOn(lightGreen, ColorRGBA.Green, greenNode, "greenMesh1");
                }                
            }
        } else  if (name.equals("ToogleBlue")) {
            if (isPressed) {
                if (getLightState(lightBlue) == 1.0) {
                    lightOff(lightBlue, blueNode, "blueMesh1");
                } else {
                    lightOn(lightBlue, ColorRGBA.Blue, blueNode, "blueMesh1");
                }                
            }
        } else  if (name.equals("Stimulate")) {
            if (isPressed) {
                stimulate = true;
            }
        }
    }
    
    public static void main(String args[]) {
        startApp(null);
    }
}
